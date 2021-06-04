package com.androidhuman.example.simplegithub.ui.main

import androidx.lifecycle.ViewModel
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.extensions.runOnIoScheduler
import com.androidhuman.example.simplegithub.util.Supportoptional
import com.androidhuman.example.simplegithub.util.emptyOptinal
import com.androidhuman.example.simplegithub.util.optionalOf
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

// SearchHistoryDao를 생성자의 인자로 받습니다.
class MainViewModel(val searchHistoryDao: SearchHistoryDao) : ViewModel() {

    // 데이터베이스에 저장되어 있는 저장소 조회 기록을 Flowable 형태로 제공합니다.
    // searchHistory 자체가 값을 갖지 않고, searchHistoryDao를 통해 데이터를 가져오므로
    // 지원 프로퍼티(backing property) 형태로 선언합니다.
    val searchHistory: Flowable<Supportoptional<List<GithubRepo>>>
        get() = searchHistoryDao.getHistory()
                // SupportOptional 형태로 데이터를 감싸줍니다.
                .map { optionalOf(it) }
                // 매 이벤트가 발생할 때마다 함수 블록을 호출합니다.
                .doOnNext { optional ->
                    if (optional.value.isEmpty()) {
                        // 표시할 데이터가 없는 경우, message 서브젝트를 통해 표시할 메시지를 전달합니다.
                        message.onNext(optionalOf("No recent repositories."))
                    } else {
                        // 데이터가 있는 경우, 메시지를 표시하지 않도록 합니다.
                        message.onNext(emptyOptinal())
                    }
                }
                // 에러가 발생했을 때 실행할 함수 블록을 정의합니다.
                .doOnError {
                    // 에러 메시지를 message 서브젝트를 통해 전달합니다.
                    message.onNext(optionalOf(it.message ?: "Unexpected error"))
                }
                // 에러가 발생한 경우 빈 데이터를 반환합니다.
                .onErrorReturn { emptyOptinal() }

    // 메시지를 전달할 서브젝트입니다.
    val message: BehaviorSubject<Supportoptional<String>> = BehaviorSubject.create()

    // 데이터베이스에 저장된 저장소 조회 기록을 모두 삭제합니다.
    fun clearSearchHistory(): Disposable
            = runOnIoScheduler { searchHistoryDao.clearAll() }
}