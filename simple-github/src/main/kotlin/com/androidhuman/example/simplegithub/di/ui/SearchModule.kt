package com.androidhuman.example.simplegithub.di.ui

import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter
import com.androidhuman.example.simplegithub.ui.search.SearchViewModelFactory
import dagger.Module
import dagger.Provides

// 모듈 클래스로 표시합니다.
@Module
class SearchModule {

    // SearchAdapter 객체를 제공합니다.
    @Provides
    fun provideAdapter(activity: SearchActivity): SearchAdapter
            = SearchAdapter().apply { setItemClickListener(activity) }

    // SearchViewModelFactory 객체를 제공합니다.
    @Provides
    fun provideViewModelFactory(
            githubApi: GithubApi, searchHistoryDao: SearchHistoryDao)
            : SearchViewModelFactory
            = SearchViewModelFactory(githubApi, searchHistoryDao)
}