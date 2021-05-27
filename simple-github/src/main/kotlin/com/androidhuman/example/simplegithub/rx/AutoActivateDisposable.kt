package com.androidhuman.example.simplegithub.rx

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.rxjava3.disposables.Disposable

class AutoActivateDisposable(
                            // 생명주기 이벤트를 받을 인스턴스입니다.
                            private val lifecycleOwner: LifecycleOwner,
                            // 이벤트를 받을 디스포저블 객체를 만드는 함수입니다.
                            private val func: () -> Disposable)
    : LifecycleObserver {

    private var disposable: Disposable? = null

    // onStart() 콜백 함수가 호출되면 activate() 함수를 실행합니다.
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun activate() {
        // 디스포저블로부터 이벤트를 받기 시작합니다.
        disposable = func.invoke()
    }

    // onStop() 콜백 함수가 호출되면 deactivate() 함수를 샐행합니다.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun deactivate() {
        // 디스포저블로부터 이벤트를 받는 것을 중단합니다.
        disposable?.dispose()
    }

    // onDestroy() 콜백 함수가 호출되면 detachSelf() 함수를 실행합니다.
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun detachSelf() {
        // 생명주기 이벤트를 더 이상 받지 않도록 옵서버에서 제거합니다.
        lifecycleOwner.lifecycle.removeObserver(this)
    }

}


