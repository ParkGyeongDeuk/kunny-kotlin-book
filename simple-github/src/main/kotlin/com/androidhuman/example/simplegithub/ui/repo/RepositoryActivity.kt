package com.androidhuman.example.simplegithub.ui.repo

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.databinding.ActivityRepositoryBinding
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import com.androidhuman.example.simplegithub.ui.GlideApp
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// AppCompatActivity 대신 DaggerAppCompatActivity를 상속합니다.
class RepositoryActivity : DaggerAppCompatActivity() {

    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    private lateinit var binding: ActivityRepositoryBinding

    // 여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화합니다.
//    internal var repoCall: Call<GithubRepo>? = null
//    internal val disposables = CompositeDisposable()
    // CompositeDisposable에서 AutoClearedDisposable로 변경합니다.
    internal val disposables = AutoClearedDisposable(this)
    // 액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가합니다.
    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)
    // RepositoryViewModel을 생성하기 위해 필요한 뷰모델 팩토리 클래스의 인스턴스를 생성합니다.
    internal val viewModelFactory by lazy {
        // 대거를 통해 주입받은 객체를 생성자의 인자로 전달합니다.
        RepositoryViewModelFactory(githubApi)
    }
    // 뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언합니다.
    lateinit var viewModel: RepositoryViewModel

    internal val dateFormatInResponse = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
    internal val dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    // 대거를 통해 GithubApi를 주입받는 프로퍼티를 선언합니다.
    @Inject lateinit var githubApi: GithubApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepositoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RepositoryViewModel의 인스턴스를 받습니다.
        viewModel = ViewModelProvider(this, viewModelFactory)[RepositoryViewModel::class.java]

        // Lifecycle.addObserver() 함수를 사용하여 AutoClearedDisposable 객체를 옵서버로 등록합니다.
        lifecycle += disposables
        // viewDisposables에서 이 액티비티의 생명주기 이벤트를 받도록 합니다.
        lifecycle += viewDisposables

        viewDisposables += viewModel.repository
                .filter { !it.isEmpty }
                .map { it.value }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repository ->
                    GlideApp.with(this@RepositoryActivity)
                            .load(repository.owner.avatarUrl)
                            .into(binding.ivActivityRepositoryProfile)

                    binding.tvActivityRepositoryName.text = repository.fullName
                    binding.tvActivityRepositoryStars.text = resources.getQuantityString(R.plurals.star, repository.stars, repository.stars)
                    if (null == repository.description) {
                        binding.tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                    } else {
                        binding.tvActivityRepositoryDescription.text = repository.description
                    }
                    if (null == repository.language) {
                        binding.tvActivityRepositoryLanguage.setText(R.string.no_language_specified)
                    } else {
                        binding.tvActivityRepositoryLanguage.text = repository.language
                    }

                    try {
                        val lastUpdate = dateFormatInResponse.parse(repository.updatedAt)
                        binding.tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(lastUpdate)
                    } catch (e: ParseException) {
                        binding.tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                    }
                }

        // 메시지 이벤트를 구독합니다.
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                // 메시지 이벤트를 받으면 화면에 해당 메시지를 표시합니다.
                .subscribe { message -> showError(message) }

        // 저장소 정보를 보여주는 뷰의 표시 유무를 결정하는 이벤트를 구독합니다.
        viewDisposables += viewModel.isContenVisible
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { visible -> setContentVisibility(visible) }

        // 작업 진행 여부 이벤트를 구독합니다.
        viewDisposables += viewModel.isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isLoading ->
                    // 작업 진행 여부 이벤트에 따라 프로그레스바의 표시 상태를 변경합니다.
                    if (isLoading) {
                        showProgress()
                    } else {
                        hideProgress()
                    }
                }

        val login = intent.getStringExtra(KEY_USER_LOGIN)
                ?: throw IllegalArgumentException("No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME)
                ?: throw IllegalArgumentException("No repo info exists in extras")

        disposables += viewModel.requestRepositoryInfo(login, repo)
    }

    /**
     * lifecycle 사용으로 onStop() 함수는 더 이상 오버라이드 하지 않아도 됩니다.
     */
//    override fun onStop() {
//        super.onStop()
//
//        // 관리하고 있던 디스포저블 객체를 모두 해체합니다.
////        repoCall?.run { cancel() }
//        disposables.clear()
//    }

    private fun showProgress() {
        binding.pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.pbActivityRepository.visibility = View.GONE
    }

    private fun setContentVisibility(show: Boolean) {
        binding.clActivityRepositoryContent.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        with(binding.tvActivityRepositoryMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

}