package com.androidhuman.example.simplegithub.ui.search

import androidx.lifecycle.ViewModel
import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.extensions.runOnIoScheduler
import com.androidhuman.example.simplegithub.util.Supportoptional
import com.androidhuman.example.simplegithub.util.emptyOptinal
import com.androidhuman.example.simplegithub.util.optionalOf
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * 생성자의 인자로 API 및 데이터베이스 접근에 필요한 인스턴스를 받습니다.
 */
class SearchViewModel(val api: GithubApi, val searchHistoryDao: SearchHistoryDao) : ViewModel() {

    // 검색 결과를 전달할 서브젝트입니다. 초깃값으로 빈 값을 지정합니다.
    val searchResult: BehaviorSubject<Supportoptional<List<GithubRepo>>>
            = BehaviorSubject.createDefault(emptyOptinal())

    // 마지막 검색어를 전달할 서브젝트입니다. 초깃값으로 빈 값을 지정합니다.
    val lastSearchKeyword: BehaviorSubject<Supportoptional<String>>
            = BehaviorSubject.createDefault(emptyOptinal())

    // 화면에 표시할 메시지를 전달할 서브젝트입니다.
    val message: BehaviorSubject<Supportoptional<String>> = BehaviorSubject.create()

    // 작업 진행 상태를 전달할 서브젝트입니다. 초깃값으로 false를 지정합니다.
    val isLoading: BehaviorSubject<Boolean>
            = BehaviorSubject.createDefault(false)

    // 검색 결과를 요청합니다.
    fun searchRepository(query: String): Disposable
            = api.searchRepository(query)
            // 검색어를 lastSearchKeyword 서브젝트에 전달합니다.
            .doOnNext { lastSearchKeyword.onNext(optionalOf(query)) }
            .flatMap {
                if (0 == it.totalCount) {
                    Observable.error(IllegalStateException("No search result"))
                } else {
                    Observable.just(it.items)
                }
            }
            // 검색을 시작하기 전에, 현재 화면에 표시되고 있던 검색 겨로가 및 메시지를 모두 제거합니다.
            // 작업 진행 상태를 true로 변경합니다.
            .doOnSubscribe {
                searchResult.onNext(emptyOptinal())
                message.onNext(emptyOptinal())
                isLoading.onNext(true)
            }
            // 작업이 종료되면(정상 종료, 오류 모두 포함) 작업 진행 상태를 false로 변경합니다.
            .doOnTerminate { isLoading.onNext(false) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                // 검색 결과를 searchResult 서브젝트에 전달합니다.
                searchResult.onNext(optionalOf(items))
            }) {
                // 에러가 발생한 경우 message 서브젝트를 통해 에러 메시지를 전달합니다.
                message.onNext(optionalOf(it.message ?: "Unexpected error"))
            }

    // 데이터베이스에 저장소 정보를 추가합니다.
    fun addToSearchHistory(repository: GithubRepo): Disposable
            = runOnIoScheduler { searchHistoryDao.add(repository) }

}