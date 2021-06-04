package com.androidhuman.example.simplegithub.ui.repo

import androidx.lifecycle.ViewModel
import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.util.Supportoptional
import com.androidhuman.example.simplegithub.util.optionalOf
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * API 호출에 필요한 객체를 생성자의 인자로 전달받습니다.
 */
class RepositoryViewModel(val api: GithubApi) : ViewModel() {

    // 저장소 정보를 전달할 서브젝트입니다.
    val repository: BehaviorSubject<Supportoptional<GithubRepo>>
            = BehaviorSubject.create()
    // 에러 메시지를 전달할 서브젝트입니다.
    val message: BehaviorSubject<String> = BehaviorSubject.create()
    // 저장소 정보를 보여주는 레이아웃의 표시 여부를 전달할 서브젝트입니다.
    // 초깃값으로 false를 지정합니다.
    val isContenVisible: BehaviorSubject<Boolean>
            = BehaviorSubject.createDefault(false)
    // 작업 진행 상태를 전달할 서브젝트입니다.
    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()

    // API를 사용하여 저장소 정보를 요청합니다.
    fun requestRepositoryInfo(login: String, repoName: String): Disposable {
        val repoObservable = if (!repository.hasValue()) {
            // repository 서브젝트에 저장된 값이 없는 경우에만
            // API를 통해 저장소 정보를 요청합니다.
            api.getRepository(login, repoName)
        } else {
            // repository 서브젝트에 저장소 정보가 있는 경우
            // 추가로 저장소 정보를 요청하지 않아도 됩니다.
            // 따라서 더 이상 작업을 진행하지 않도록 Observable.empty()를 반환합니다.
            Observable.empty()
        }
        return repoObservable
                // 저장소 정보를 받기 시작하면 작업 진행 상태를 true로 변경합니다.
                .doOnSubscribe { isLoading.onNext(true) }
                // 작업이 완료되면(오류, 정상 종료 퐇ㅁ) 작업 진행 상태를 false로 변경합니다.
                .doOnTerminate { isLoading.onNext(false) }
                .subscribeOn(Schedulers.io())
                .subscribe({ repo ->
                    // repository 서브젝트에 저장소 정보를 전달합니다.
                    repository.onNext(optionalOf(repo))
                    // 저장소 정보를 보여주는 뷰를 화면에 보여주기 위해
                    // isContentVisible 서브젝트에 이벤트를 전달합니다.
                    isContenVisible.onNext(true)
                }) {
                    // 에러가 발생하면 message 서브젝트에 에러 메시지를 전달합니다.
                    message.onNext(it.message ?: "Unexpected error")
                }
    }

}