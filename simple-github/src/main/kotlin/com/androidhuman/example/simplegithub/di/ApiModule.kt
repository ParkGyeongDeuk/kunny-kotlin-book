package com.androidhuman.example.simplegithub.di

import com.androidhuman.example.simplegithub.api.AuthApi
import com.androidhuman.example.simplegithub.api.GithubApi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

// 모듈 클래스로 표시합니다.
@Module
class ApiModule {

    // AuthApi 객체를 제공합니다.
    // 이 객체를 생성할 때 필요한 객체들은 함수의 인자로 선언합니다.
    @Provides
    @Singleton
    fun provideAuthApi(
            // 인증 토큰을 추가하지 않는 OkHttpClient 객체를
            // "unauthorized"라는 이름으로 구분합니다.
            @Named("unauthorized") okHttpClient: OkHttpClient,
            callAdapter: CallAdapter.Factory,
            converter: Converter.Factory): AuthApi
            = Retrofit.Builder()
            .baseUrl("https://github.com")
            .client(okHttpClient)
            .addCallAdapterFactory(callAdapter)
            .addConverterFactory(converter)
            .build()
            .create(AuthApi::class.java)

    // GithubApi 객체를 제공합니다.
    // 이 객체를 생성할 때 필요한 객체들은 함수의 인자로 선언합니다.
    @Provides
    @Singleton
    fun provideGithubApi(
            // 인증 토큰을 추가하는 OkHttpClient 객체를
            // "authorized"라는 이름으로 구분합니다.
            @Named("authorized") okHttpClient: OkHttpClient,
            callAdapter: CallAdapter.Factory,
            converter: Converter.Factory): GithubApi
            = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .client(okHttpClient)
            .addCallAdapterFactory(callAdapter)
            .addConverterFactory(converter)
            .build()
            .create(GithubApi::class.java)

    // CallAdapter.Factory 객체를 제공합니다.
    @Provides
    @Singleton
    fun provideCallAdapterFactory(): CallAdapter.Factory
            = RxJava3CallAdapterFactory.create()

    // Converter.Factory 객체를 제공합니다.
    @Provides
    @Singleton
    fun provideConverterFactory(): Converter.Factory
            = GsonConverterFactory.create()

}