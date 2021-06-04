package com.androidhuman.example.simplegithub.util

// SupportOptional 클래스는 하나의 값을 포함할 수 있습니다.
// Empty와 Some 클래스만 SupportOptional 클래스를 상속할 수 있도록 sealed class로 선언합니다.
sealed class Supportoptional<out T : Any>(private val _value: T?) {

    // 클래스가 널이 아닌 값을 가지고 있는지 여부를 반환한다.
    val isEmpty: Boolean get() = null == _value

    // 값을 반환하기 전에 checkNotNull() 함수를 사용하여 널 여부를 확인합니다.
    // 널 값을 반환하려 하는 경우 IllegalStateException이 발생합니다.
    val value: T get() = checkNotNull(_value)
}

// 빈 값을 표시하기 위한 클래스입니다.
class Empty<out T : Any> : Supportoptional<T>(null)

// 널 값이 아닌 데이터를 표시하기 위한 클래스입니다.
class Some<out T : Any>(value: T) : Supportoptional<T>(value)

// SupportOptional 형태로 데이터를 감싸는 유틸리티 함수입니다.
// 널 값이 아니라면 Some 클래스로, 널 값이라면 Empty 클래스로 감싸줍니다.
inline fun <reified T : Any> optionalOf(value: T?) = if (null != value) Some(value) else Empty<T>()

// Empty 클래스의 인스턴스를 간편하게 만들어주는 유틸리티 함수입니다.
inline fun <reified T : Any> emptyOptinal() = Empty<T>()