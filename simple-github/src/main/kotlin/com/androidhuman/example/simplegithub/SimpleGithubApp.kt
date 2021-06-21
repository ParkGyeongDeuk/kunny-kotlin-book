package com.androidhuman.example.simplegithub

import com.androidhuman.example.simplegithub.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

// DaggerApplication을 상속합니다.
class SimpleGithubApp : DaggerApplication() {

    // DaggerAppComponent의 인스턴스를 반환합니다.
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }
}