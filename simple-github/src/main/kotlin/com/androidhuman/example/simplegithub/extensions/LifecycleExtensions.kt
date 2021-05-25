package com.androidhuman.example.simplegithub.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver

// Lifecycle 클래스의 '+=' 연산자를 오버로딩합니다.
operator fun Lifecycle.plusAssign(observer: LifecycleObserver) = this.addObserver(observer)