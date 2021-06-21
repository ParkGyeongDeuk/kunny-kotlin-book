package com.androidhuman.example.simplegithub.di.ui

import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.ui.repo.RepositoryViewModelFactory
import dagger.Module
import dagger.Provides

// 모듈 클래스로 표시합니다.
@Module
class RepositoryModule {

    // RepositoryViewModelFactory 객체를 제공합니다.
    @Provides
    fun provideViewModelFactory(githubApi: GithubApi)
            : RepositoryViewModelFactory
            = RepositoryViewModelFactory(githubApi)
}