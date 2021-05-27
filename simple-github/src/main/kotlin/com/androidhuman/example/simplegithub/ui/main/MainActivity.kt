package com.androidhuman.example.simplegithub.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.data.provideSearchHistoryDao
import com.androidhuman.example.simplegithub.databinding.ActivityMainBinding
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.extensions.runOnIoScheduler
import com.androidhuman.example.simplegithub.rx.AutoActivateDisposable
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers.io
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {

    private lateinit var binding: ActivityMainBinding
    // 어댑터 프로퍼티를 추가합니다.
    internal val adapter by lazy { SearchAdapter().apply { setItemClickListener(this@MainActivity) } }
    // 최근 조회한 저장소를 담당하는 데이터 접근 객체 프로퍼티를 추가합니다.
    internal val searchHistoryDao by lazy { provideSearchHistoryDao(this) }
    // 디스포저블을 관리하는 프로퍼티를 추가합니다.
    internal val disposables = AutoClearedDisposable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 생명주기 이벤트 옵서버를 등록합니다.
        lifecycle += disposables
        lifecycle += AutoActivateDisposable(this) { fetchSearchHistory() }

        binding.btnActivityMainSearch.setOnClickListener {
            startActivity<SearchActivity>()
        }

        // 리사이클러뷰에 어댑터를 설정합니다.
        with(binding.rvActivityMainList) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 'Clear all' 메뉴를 선택하면 조회했던 저장소 기록을 모두 삭제합니다.
        if (R.id.menu_activity_main_clear_all == item.itemId) {
            clearAll()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    /**
     * 데이터베이스에 저장되어 있는 저장소 목록을 불러오는 작업을 반환합니다.
     * SearchHistoryDao.getHistory() 함수는 Flowable 형태로 데이터를 반환하므로,
     * 데이터베이스에 저장된 자료가 바뀌면 즉시 업데이트된 정보가 새로 전달됩니다.
     */
    private fun fetchSearchHistory(): Disposable
            = searchHistoryDao.getHistory()
            // 메인 스레드에서 호출하면 Room에서 오류를 발생시키므로 IO 스레드에서 작업을 수행합니다.
            .subscribeOn(io())
            // 결과를 받아 뷰에 업데이트해야 하므로 메인 스레드(UI 스레드)에서 결과를 처리합니다.
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                // 어댑터를 갱신합니다.
                with(adapter) {
                    setItems(items)
                    notifyDataSetChanged()
                }
                // 저장된 데이터의 유무에 따라 오류 메시지를 표시하거나 감춥니다.
                if (items.isEmpty()) {
                    showMessage(getString(R.string.no_recent_repositories))
                } else {
                    hideMessage()
                }
            }) {
                showMessage(it.message)
            }

    /**
     * 데이터베이스에 저장되어 있는 모든 저장소 기록을 삭제합니다.
     */
    private fun clearAll() {
        // 메인 스레드에서 실행하면 오류가 발생하므로,
        // 앞에서 작성한 runOnIoScheduler() 함수를 사용하여 IO 스레드에서 작업을 실행합니다.
        disposables += runOnIoScheduler { searchHistoryDao.clearAll() }
    }

    private fun showMessage(message: String?) {
        with(binding.tvActivityMainMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

    private fun hideMessage() {
        with(binding.tvActivityMainMessage) {
            text = ""
            visibility = View.GONE
        }
    }

}