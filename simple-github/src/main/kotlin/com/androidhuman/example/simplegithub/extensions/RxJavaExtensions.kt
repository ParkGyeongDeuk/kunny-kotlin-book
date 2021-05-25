package com.androidhuman.example.simplegithub.extensions

import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable


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