package com.androidhuman.example.simplegithub.di

import com.androidhuman.example.simplegithub.api.AuthInterceptor
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Named
import javax.inject.Singleton

// 모듈 클래스로 표시합니다.
@Module
class NetworkModule {

    @Provides
    @Named("unauthorized")
    @Singleton
    fun provideUnauthorizedOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor): OkHttpClient
            = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Named("authorized")
    @Singleton
    fun provideAuthorizedOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            authInterceptor: AuthInterceptor): OkHttpClient
            = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    // HttpLoggingInterceptor 객체를 제공합니다.
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor
            = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
    }

    // AuthInterceptor 객체를 제공합니다.
    @Provides
    @Singleton
    fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
        val token = provider.token?: throw IllegalStateException("authToken cannot be null")
        return AuthInterceptor(token)
    }
}