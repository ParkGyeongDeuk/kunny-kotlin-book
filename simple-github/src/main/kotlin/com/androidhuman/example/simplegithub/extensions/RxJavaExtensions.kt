package com.androidhuman.example.simplegithub.extensions

import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers.io


/**
 * CompositeDisposable의 '+=' 연산자 뒤에 Disposable 타입이 오는 경우를 재정의 합니다.
 */
//operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
//
//    // CompositeDisposable.add() 함수를 호출합니다.
//    this.add(disposable)
//}

/**
 * CompositeDisposable.plusAssign() 대신 사용하도록 변경
 */
operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) = this.add(disposable)

fun runOnIoScheduler(func: () -> Unit): Disposable
        = Completable.fromCallable(func)
        .subscribeOn(io())
        .subscribe()