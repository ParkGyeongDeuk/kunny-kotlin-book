package com.androidhuman.example.simplegithub.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.databinding.ActivityMainBinding
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.rx.AutoActivateDisposable
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers.io
import org.jetbrains.anko.startActivity
import javax.inject.Inject

// AppCompatActivity 대신 DaggerAppCompatActivity를 상속합니다.
class MainActivity : DaggerAppCompatActivity(), SearchAdapter.ItemClickListener {

    private lateinit var binding: ActivityMainBinding
    // 디스포저블을 관리하는 프로퍼티를 추가합니다.
    internal val disposables = AutoClearedDisposable(this)
    // 액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가합니다.
    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)
    // 대거로부터 SearchAdapter 객체를 주입받습니다.
    @Inject lateinit var adapter: SearchAdapter
    // 대거로부터 MainViewModelFactory 객체를 주입받습니다.
    @Inject lateinit var viewModelFactory: MainViewModelFactory
    // 뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언합니다.
    lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // viewModel의 인스턴스를 받는다.
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        // 생명주기 이벤트 옵서버를 등록합니다.
        lifecycle += disposables
        // viewDisposables에서 이 액티비티의 생명주기 이벤트를 받도록 합니다.
        lifecycle += viewDisposables
        // 액티비티가 활성 상태일 때만 데이터베이스에 저장된 저장소 조회 기록을 받도록 합니다.
        lifecycle += AutoActivateDisposable(this) {
            viewModel.searchHistory
                    .subscribeOn(io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { items ->
                        with(adapter) {
                            if (items.isEmpty) {
                                clearItems()
                            } else {
                                setItems(items.value)
                            }
                            notifyDataSetChanged()
                        }
                    }
        }

        binding.btnActivityMainSearch.setOnClickListener {
            startActivity<SearchActivity>()
        }

        // 리사이클러뷰에 어댑터를 설정합니다.
        with(binding.rvActivityMainList) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        // 메시지 이벤트를 구독합니다.
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message ->
                    if (message.isEmpty) {
                        // 빈 메시지를 받은 경우 표시되고 있는 메시지를 화면에서 숨깁니다.
                        hideMessage()
                    } else {
                        // 유효한 메시지를 받은 경우 화면에 메시지를 표시합니다.
                        showMessage(message.value)
                    }
                }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 'Clear all' 메뉴를 선택하면 조회했던 저장소 기록을 모두 삭제합니다.
        if (R.id.menu_activity_main_clear_all == item.itemId) {
            // 데이터베이스에 저장된 저장소 조회 기록 데이터를 모두 삭제합니다.
            disposables += viewModel.clearSearchHistory()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
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