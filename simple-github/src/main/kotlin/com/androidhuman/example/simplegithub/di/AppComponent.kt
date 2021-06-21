package com.androidhuman.example.simplegithub.di

import android.app.Application
import com.androidhuman.example.simplegithub.SimpleGithubApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

// AppComponent를 선언하면서 컴포넌트로 묶어둘 모듈을 추가합니다.
// 대거의 안드로이드 지원 모듈인 AndroidSupportInjectionModule을 함께 추가합니다.
// AppComponent는 AndroidInjector 인터페이스를 상속하도록 하며,
// 애플리케이션을 상속한 클래스인 SimpleGithubApp을 타입 인자로 넣어줍니다.
@Singleton
@Component(
        modules = arrayOf(
                AppModule::class,
                LocalDataModule::class,
                ApiModule::class,
                NetworkModule::class,
                AndroidSupportInjectionModule::class,
                ActivityBinder::class))

interface AppComponent : AndroidInjector<SimpleGithubApp> {
    // AppComponent를 생성할 때 사용할 빌더 클래스를 정의합니다.
    @Component.Builder
    interface Builder {
        // @BindsInstence 어노테이션으로 객체 그래프에 추가할 객체를 선언합니다.
        // 객체 그래프에 추가할 객체를 인자로 받고, 빌더 클래스에 반환하는 함수 형태로 선언합니다.
        @BindsInstance
        fun application(app: Application): Builder

        // 빌더 클래스는 컴포넌트를 반환하는 build()함수를 반드시 포함해야 합니다.
        fun build(): AppComponent
    }

}