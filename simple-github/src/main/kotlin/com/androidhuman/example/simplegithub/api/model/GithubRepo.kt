package com.androidhuman.example.simplegithub.api.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// GithubRepo 엔티티의 데이터가 저장될 테이블 이름을 repositories로 지정합니다.
@Entity(tableName = "repositories")

class GithubRepo(
        val name: String,
        @SerializedName("full_name")
        // fullName 프로퍼티를 주요 키로 사용하며, 테이블 내 필드 이름은 full_name으로 지정합니다.
        @PrimaryKey @ColumnInfo(name = "full_name") val fullName: String,
        // GithubOwner 내 필드를 테이블에 함께 저장합니다.
        @Embedded val owner: GithubOwner,
        val description: String?,
        val language: String?,
        @SerializedName("updated_at")
        // updatedAt 프로퍼티의 테이블 내 필드 이름을 updatd_at으로 지정합니다.
        @ColumnInfo(name = "updated_at") val updatedAt: String,
        @SerializedName("stargazers_count") val stars: Int)