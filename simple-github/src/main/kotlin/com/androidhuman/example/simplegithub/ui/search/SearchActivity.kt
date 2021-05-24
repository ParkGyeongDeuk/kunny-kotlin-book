package com.androidhuman.example.simplegithub.ui.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.api.model.RepoSearchResponse
import com.androidhuman.example.simplegithub.api.provideGithubApi
import com.androidhuman.example.simplegithub.databinding.ActivitySearchBinding
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter.ItemClickListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var binding: ActivitySearchBinding

    internal lateinit var menuSearch: MenuItem
    internal lateinit var searchView: SearchView

    internal val adapter by lazy { SearchAdapter().apply { setItemClickListener(this@SearchActivity) } }

    internal val api by lazy { provideGithubApi(this) }

    // 여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화합니다.
//    internal var searchCall: Call<RepoSearchResponse>? = null 대신 사용합니다.
    internal val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.rvActivitySearchList) {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
        }
    }

    override fun onStop() {
        super.onStop()

        // 관리하고 있던 디스포저블 객체를 모두 해제합니다.
//        searchCall?.run { cancel() } 대신 사용합니다.
        disposables.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menu_activity_search_query)

        searchView = (menuSearch.actionView as SearchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }

        with(menuSearch) {
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                    if ("" == searchView.query) {
                        finish()
                    }
                    return true
                }
            })

            expandActionView()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.menu_activity_search_query == item.itemId) {
            item.expandActionView()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    private fun searchRepository(query: String) {

        // REST API를 통해 검색 결과를 요청합니다.
        // '+=' 연산자로 디스포저블을 CompositeDisposable에 추가합니다.
        disposables += api.searchRepository(query)

                // Observable 형태로 결과를 바꿔주기 위해 flatMap을 사용합니다.
                .flatMap {
                    if (0 == it.totalCount) {
                        // 검색 결과가 없을 경우 에러를 발생시켜서 에러 메시지를 표시하도록 합니다.
                        // (곧바로 에러 블록이 실행됩니다.)
                        Observable.error(IllegalStateException("No search result"))
                    } else {
                        Observable.just(it.items)
                    }
                }

                // 이 이후에 수행되는 코드는 모두 메인 스레드에서 실행합니다.
                // RxAndroid에서 제공하는 스케줄러인 AndroidSchedulers.mainThread()를 사용합니다.
                .observeOn(AndroidSchedulers.mainThread())

                // 구독할 때 수행할 작업을 구현합니다.
                .doOnSubscribe {
                    clearResults()
                    hideError()
                    showProgress()
                }

                // 스트림이 종료될 때 수행할 작업을 구현합니다.
                .doOnTerminate { hideProgress() }

                // 옵서버블을 구독합니다.
                .subscribe({ items ->

                    // API를 통해 검색 결과를 정상적으로 받았을 때 처리할 작업을 구현합니다.
                    // 작업 중 오류가 발생하면 이 블록은 호출되지 않습니다.
                    with(adapter) {
                        setItems(items)
                        notifyDataSetChanged()
                    }
                }) {
                    // 에러 블록
                    // 네트워크 오류나 데이터 처리 오류 등
                    // 작업이 정상적으로 완료되지 않았을 때 호출됩니다.
                    showError(it.message)
                }

    }

    private fun updateTitle(query: String) {
        supportActionBar?.run { subtitle = query }
    }

    private fun hideSoftKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).run {
            hideSoftInputFromWindow(searchView.windowToken, 0)
        }
    }

    private fun collapseSearchView() {
        menuSearch.collapseActionView()
    }

    private fun clearResults() {
        with(adapter) {
            clearItems()
            notifyDataSetChanged()
        }
    }

    private fun showProgress() {
        binding.pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String?) {
        with(binding.tvActivitySearchMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        with(binding.tvActivitySearchMessage) {
            text = ""
            visibility = View.GONE
        }
    }
}