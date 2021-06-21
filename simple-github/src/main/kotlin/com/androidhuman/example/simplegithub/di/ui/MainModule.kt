package com.androidhuman.example.simplegithub.di.ui

import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import com.androidhuman.example.simplegithub.ui.main.MainViewModelFactory
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter
import dagger.Module
import dagger.Provides

// 모듈 클래스로 표시합니다.
@Module
class MainModule {

    // SearchAdapter 객체를 제공합니다.
    @Provides
    fun provideAdapter(activity: MainActivity): SearchAdapter
            = SearchAdapter().apply { setItemClickListener(activity) }

    // MainViewModelFactory 객체를 제공합니다.
    @Provides
    fun provideViewModelFactory(searchHistoryDao: SearchHistoryDao): MainViewModelFactory
            = MainViewModelFactory(searchHistoryDao)
}