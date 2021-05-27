package com.androidhuman.example.simplegithub.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidhuman.example.simplegithub.api.model.GithubRepo

// 데이터베이스에서 사용하는 엔티티와 버전을 지정합니다.
@Database(entities = arrayOf(GithubRepo::class), version = 1)
abstract class SimpleGithubDatabase : RoomDatabase() {

    // 데이터베이스와 연결할 데이터 접근 객체를 정의합니다.
    abstract fun searchHistoryDao(): SearchHistoryDao
}