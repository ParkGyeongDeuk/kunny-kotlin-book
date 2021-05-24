package com.androidhuman.example.simplegithub.ui.repo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.api.provideGithubApi
import com.androidhuman.example.simplegithub.databinding.ActivityRepositoryBinding
import com.androidhuman.example.simplegithub.ui.GlideApp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RepositoryActivity : AppCompatActivity() {

    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    private lateinit var binding: ActivityRepositoryBinding

    internal val api by lazy { provideGithubApi(this) }

    // 여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화합니다.
//    internal var repoCall: Call<GithubRepo>? = null
    internal val disposables = CompositeDisposable()

    internal val dateFormatInResponse = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
    internal val dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepositoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val login = intent.getStringExtra(KEY_USER_LOGIN)
                ?: throw IllegalArgumentException("No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME)
                ?: throw IllegalArgumentException("No repo info exists in extras")

        showRepositoryInfo(login, repo)
    }

    override fun onStop() {
        super.onStop()

        // 관리하고 있던 디스포저블 객체를 모두 해체합니다.
//        repoCall?.run { cancel() }
        disposables.clear()
    }

    private fun showRepositoryInfo(login: String, repoName: String) {

        // REST API를 통해 저장소 정보를 요청합니다.
        disposables.add(api.getRepository(login, repoName)

                // 이 이후에 수행되는 코드는 모두 메인 스레드에서 실행합니다.
                .observeOn(AndroidSchedulers.mainThread())

                // 구독할 때 수행할 작업을 구현합니다.
                .doOnSubscribe { showProgress() }

                // 에러가 발생했을 때 수행할 작업을 구현합니다.
                .doOnError { hideProgress(false) }

                // 스트림이 정상 종료되었을 때 수행할 작업을 구현합니다.
                .doOnComplete { hideProgress(true) }

                // 옵서버블을 구독합니다.
                .subscribe({ repo ->

                    // API를 통해 저장소 정보를 정상적으로 받았을 때 처리할 작업을 구현합니다.
                    // 작업 중 오류가 발생하면 이 블록은 호출되지 않습니다.
                    GlideApp.with(this@RepositoryActivity)
                            .load(repo.owner.avatarUrl)
                            .into(binding.ivActivityRepositoryProfile)

                    binding.tvActivityRepositoryName.text = repo.fullName
                    binding.tvActivityRepositoryStars.text = resources
                            .getQuantityString(R.plurals.star, repo.stars, repo.stars)

                    if (null == repo.description) {
                        binding.tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                    } else {
                        binding.tvActivityRepositoryDescription.text = repo.description
                    }
                    if (null == repo.language) {
                        binding.tvActivityRepositoryLanguage.setText(R.string.no_language_specified)
                    } else {
                        binding.tvActivityRepositoryLanguage.text = repo.language
                    }

                    try {
                        val lastUpdate = dateFormatInResponse.parse(repo.updatedAt)
                        binding.tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(lastUpdate)
                    } catch (e: ParseException) {
                        binding.tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                    }
                }) {
                    // 에러 블록
                    // 네트워크 오류나 데이터 처리 오류 등 작업이 정상적으로 완료되지 않았을 때 호출됩니다.
                    showError(it.message)
                })
    }

    private fun showProgress() {
        binding.clActivityRepositoryContent.visibility = View.GONE
        binding.pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress(isSucceed: Boolean) {
        binding.clActivityRepositoryContent.visibility = if (isSucceed) View.VISIBLE else View.GONE
        binding.pbActivityRepository.visibility = View.GONE
    }

    private fun showError(message: String?) {
        with(binding.tvActivityRepositoryMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

}