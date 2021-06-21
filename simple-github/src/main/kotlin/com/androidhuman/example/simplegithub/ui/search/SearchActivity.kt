package com.androidhuman.example.simplegithub.ui.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.databinding.ActivitySearchBinding
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.jetbrains.anko.startActivity
import javax.inject.Inject

// AppCompatActivity 대신 DaggerAppCompatActivity를 상속합니다.
class SearchActivity : DaggerAppCompatActivity(), SearchAdapter.ItemClickListener {

    private lateinit var binding: ActivitySearchBinding

    internal lateinit var menuSearch: MenuItem
    internal lateinit var searchView: SearchView

    internal val adapter by lazy { SearchAdapter().apply { setItemClickListener(this@SearchActivity) } }

    // 여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화합니다.
//    internal var searchCall: Call<RepoSearchResponse>? = null 대신 사용합니다.
//    internal val disposables = CompositeDisposable()
    // CompositeDisposable에서 AutoClearedDisposable로 변경합니다.
    internal val disposables = AutoClearedDisposable(this)
    // viewDisposables 프로퍼티를 추가합니다.
//    internal val viewDisposables = CompositeDisposable()
    // CompositeDisposable에서 AutoClearedDisposable로 변경합니다.
    // 액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가합니다.
    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)
    // SearchViewModel을 생성할 때 필요한 뷰모델 팩토리 클래스의 인스턴스를 생성합니다.
    internal val viewModelFactory by lazy {
        // 대거를 통해 주입받은 객체를 생성자의 인자로 전달합니다.
        SearchViewModelFactory(githubApi, searchHistoryDao)
    }
    // 뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언합니다.
    lateinit var viewModel: SearchViewModel
    // 대거를 통해 GithubApi를 주입받는 프로퍼티를 선언합니다.
    @Inject lateinit var githubApi: GithubApi
    // 대거를 통해 SearchHistoryDao를 주입받는 프로퍼티를 선언합니다.
    @Inject lateinit var searchHistoryDao: SearchHistoryDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SearchViewModel의 인스턴스를 받습니다.
        viewModel = ViewModelProvider(this, viewModelFactory)[SearchViewModel::class.java]

        // Lifecycle.addObserver() 함수를 사용하여 각 객체를 옵서버로 등록합니다.
        lifecycle += disposables
        // viewDisposables에서 이 액티비티의 생명주기 이벤트를 받도록 합니다.
        lifecycle += viewDisposables

        with(binding.rvActivitySearchList) {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
        }

        // 검색 결과 이벤트를 구독합니다.
        viewDisposables += viewModel.searchResult
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items ->
                    with(adapter) {
                        if (items.isEmpty) {
                            // 빈 이벤트를 받으면 표시되고 있던 항목을 제거합니다.
                            clearItems()
                        } else {
                            // 유효한 이벤트를 받으면 데이터를 화면에 표시합니다.
                            setItems(items.value)
                        }
                        notifyDataSetChanged()
                    }
                }

        // 메시지 이벤트를 구독합니다.
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message ->
                    if (message.isEmpty) {
                        // 빈 이벤트를 받으면 화면에 표시되고 있던 메시지를 숨깁니다.
                        hideError()
                    } else {
                        // 유효한 이벤트를 받으면 화면에 메시지를 표시합니다.
                        showError(message.value)
                    }
                }

        // 작업 진행 여부 이벤트를 구독합니다.
        viewDisposables += viewModel.isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isLoading ->
                    // 작업 진행 여부 이벤트에 따라 프로그레스바의 표시 상태를 변경합니다.
                    if (isLoading) {
                        showProgress()
                    } else {
                        hideProgress()
                    }
                }
    }

    /**
     * lifecycle 사용으로 onStop() 함수는 더 이상 오버라이드 하지 않아도 됩니다.
     */
//    override fun onStop() {
//        super.onStop()
//        // 관리하고 있던 디스포저블 객체를 모두 해제합니다.
////        searchCall?.run { cancel() } 대신 사용합니다.
//        disposables.clear()
//        // 액티비티가 완전히 종료되고 있는 경우에만 관리하고 있는 디스포저블을 해제합니다.
//        // 화면이 꺼지거나 다른 액티비티를 호출하여 액티비티가 화면에서 사라지는 경우에는 해제하지 않습니다.
//        if (isFinishing) {
//            viewDisposables.clear()
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menu_activity_search_query)

        searchView = (menuSearch.actionView as SearchView)

        // SearchView에서 발생하는 이벤트를 옵서버블 형태로 받습니다.
        viewDisposables += searchView.queryTextChangeEvents()
                // 검색을 수행했을 때 발생한 이벤트만 받습니다.
                .filter { it.isSubmitted }
                // 이벤트에서 검색어 텍스트(CharSequence)를 추출합니다.
                .map { it.queryText }
                // 빈 문자열이 아닌 검색어만 받습니다.
                .filter { it.isNotEmpty() }
                // 검색어를 String 형태로 변환합니다.
                .map { it.toString() }
                // 이 이후에 수행되는 코드는 모두 메인 스레드에서 실행합니다.
                // RxAndroid에서 제공하는 스케줄러인 AndroidSchedulers.mainThread()를 사용합니다.
                .observeOn(AndroidSchedulers.mainThread())
                // 옵서버블을 구독합니다.
                .subscribe { query ->
                    // 검색 절차를 수행합니다.
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                }

        // 마지막으로 검색한 검색어 이벤트를 구독합니다.
        viewDisposables += viewModel.lastSearchKeyword
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { keyword ->
                    if (keyword.isEmpty) {
                        // 아직 검색을 수행하지 않은 경우 SearchView를 펼친 상태로 유지합니다.
                        menuSearch.expandActionView()
                    } else {
                        // 검색어가 있는 경우 해당 검색어를 액티비티의 제목으로 표시합니다.
                        updateTitle(keyword.value)
                    }
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

        // 선택한 저장소 정보를 데이터베이스에 추가합니다.
        disposables += viewModel.addToSearchHistory(repository)

        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    private fun searchRepository(query: String) {

        // 전달받은 검색어로 검색 결과를 요청합니다.
        disposables += viewModel.searchRepository(query)

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