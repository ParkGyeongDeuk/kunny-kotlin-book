package com.androidhuman.example.simplegithub.ui.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.androidhuman.example.simplegithub.BuildConfig
import com.androidhuman.example.simplegithub.api.provideAuthApi
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import com.androidhuman.example.simplegithub.databinding.ActivitySignInBinding
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.newTask

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    internal val api by lazy { provideAuthApi() }
    internal val authTokenProvider by lazy { AuthTokenProvider(this) }

    // 여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화합니다.
//    internal var accessTokenCall: Call<GithubAccessToken>? = null 대신 사용합니다.
    internal val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnActivitySignInStart.setOnClickListener {
            val authUri = Uri.Builder().scheme("https").authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                    .build()
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@SignInActivity, authUri)
        }

        authTokenProvider.token?.let {
            launchMainActivity()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        showProgress()

        val uri = intent.data ?: throw IllegalArgumentException("No data exists")
        val code = uri.getQueryParameter("code") ?: throw IllegalStateException("No code exists")

        getAccessToken(code)
    }

    override fun onStop() {
        super.onStop()
        // 관리하고 있던 디스포저블 객체를 모두 해제합니다.
//        accessTokenCall?.run { cancel() } 대신 사용합니다.
        disposables.clear()
    }

    private fun getAccessToken(code: String) {

        // REST API를 통해 엑세스 토큰을 요청합니다.
        // '+=' 연산자로 디스포저블을 CompositeDisposable에 추가합니다.
        disposables += api.getAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)

                // REST API를 통해 받은 응답에서 엑세스 토큰만 추출합니다.
                .map { it.accessToken }

                // 이 이후에 수행되는 코드는 모두 메인 스레드에서 실행합니다.
                // RxAndroid에서 제공하는 스케줄러인 AndroidSchedulers.mainThread()를 사용합니다.
                .observeOn(AndroidSchedulers.mainThread())

                // 구독할 때 수행할 작업을 구현합니다.
                .doOnSubscribe { showProgress() }

                // 스트림이 종료될 때 수행할 작업을 구현합니다.
                .doOnTerminate { hideProgress() }

                // 옵서버블을 구독합니다.
                .subscribe({ token ->
                    // API를 통해 엑세스 토큰을 정상적으로 받았을 때 처리할 작업을 구현합니다.
                    // 작업 중 오류가 발생하면 이 블록은 호출되지 않습니다.
                    authTokenProvider.updateToken(token)
                    launchMainActivity()
                }) {
                    // 에러 블록
                    // 네트워크 오류나 데이터 처리 오류 등
                    // 작업이 정상적으로 완료되지 않았을 때 호출됩니다.
                    showError(it)
                }
    }

    private fun showProgress() {
        binding.btnActivitySignInStart.visibility = View.GONE
        binding.pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.btnActivitySignInStart.visibility = View.VISIBLE
        binding.pbActivitySignIn.visibility = View.GONE
    }

    private fun showError(throwable: Throwable) {
        longToast(throwable.message ?: "No message available")
    }

    private fun launchMainActivity() {
        startActivity(intentFor<MainActivity>().clearTask().newTask())
    }
}