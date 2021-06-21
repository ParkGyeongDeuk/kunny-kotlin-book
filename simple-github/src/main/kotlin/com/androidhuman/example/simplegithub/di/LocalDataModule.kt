package com.androidhuman.example.simplegithub.di

import android.content.Context
import androidx.room.Room
import com.androidhuman.example.simplegithub.data.*
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

// 모듈 클래스로 표시합니다.
@Module
class LocalDataModule {

    // 인증 토큰을 관리하는 객체인 AuthTokenProvider를 제공합니다.
    // AuthTokenProvider는 SharedPreferences를 기반으로 인증 토큰을 관리합니다.
    // "appContext"라는 이름으로 구분되는 Context 객체를 필요로 합니다.
    @Provides
    @Singleton
    fun provideAuthTokenProvider(@Named("appContext") context: Context): AuthTokenProvider
            = AuthTokenProvider(context)

    // 저장소 조회 기록을 관리하는 객체인 SearchHistoryDao를 제공합니다
    @Provides
    @Singleton
    fun provideSearchHistoryDao(db: SimpleGithubDatabase): SearchHistoryDao = db.searchHistoryDao()

    // 데이터베이스를 관리하는 객체인 SimpleGithubDatabase를 제공합니다.
    // "appContext"라는 이름으로 구분되는 Context 객체를 필요로 합니다.
    @Provides
    @Singleton
    fun provideDatabase(@Named("appContext") context: Context)
            : SimpleGithubDatabase
            = Room.databaseBuilder(context, SimpleGithubDatabase::class.java, "simple_github.db")
            .build()

}