package com.androidhuman.example.simplegithub.data

import android.content.Context
import androidx.room.Room

// SimpleGithubDatabase의 인스턴스를 저장합니다.
private var instance: SimpleGithubDatabase? = null

// 저장소 조회 기록을 담당하는 데이터 접근 객체를 제공합니다.
fun provideSearchHistoryDao(context: Context): SearchHistoryDao = provideDatabase(context).searchHistoryDao()

// SimpleGithubDatabase 룸 데이터베이스를 제공합니다.
// 싱글톤 패턴을 사용하여 인스턴스를 최초 1회만 생성합니다.
private fun provideDatabase(context: Context): SimpleGithubDatabase {
    if (null == instance) {

        // simple_github.db 데이터베이스 파일을 사용하는 룸 데이터베이스를 생성합니다.
        instance = Room.databaseBuilder(context.applicationContext,
                SimpleGithubDatabase::class.java, "simple_github.db")
                .build()

    }

    // 룸 데이터베이스 인스턴스를 반환합니다.
    return instance!!
}


