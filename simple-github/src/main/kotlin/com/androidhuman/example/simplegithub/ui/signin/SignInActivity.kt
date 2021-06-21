package com.androidhuman.example.simplegithub.ui.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModelProvider
import com.androidhuman.example.simplegithub.BuildConfig
import com.androidhuman.example.simplegithub.api.AuthApi
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import com.androidhuman.example.simplegithub.databinding.ActivitySignInBinding
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.newTask
import javax.inject.Inject

// AppCompatActivity 대신 DaggerAppCompatActivity를 상속합니다.
class SignInActivity : DaggerAppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    // 여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화합니다.
//    internal var accessTokenCall: Call<GithubAccessToken>? = null 대신 사용합니다.
//    internal val disposables = CompositeDisposable()
    // CompositeDisposable에서 AutoClearedDisposable로 변경합니다.
    internal val disposables = AutoClearedDisposable(this)
    // 액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가합니다.
    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)
    // SignInViewModel을 생성할 때 필요한 뷰모델 팩토리 클래스의 인스턴스를 생성합니다.
    internal val viewModelFactory by lazy {
        // 대거를 통해 주입받은 객체를 생성자의 인자로 전달합니다.
        SignInViewModelFactory(authApi, authTokenProvider)
    }
    // 뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언한다.
    lateinit var viewModel: SignInViewModel
    // 대거를 통해 AuthApi 객체를 주입받는 프로퍼티를 선언합니다.
    // @Inject 어노테이션을 추가해야 대거로부터 객체를 주입받을 수 있습니다.
    // 선언 시점에 프로퍼티를 초기화할 수 없으므로 lateinit var로 선언압니다.
    @Inject lateinit var authApi: AuthApi
    // 대거를 통해 AuthTokenProvider 객체를 주입받는 프로퍼티를 선언합니다.
    @Inject lateinit var authTokenProvider: AuthTokenProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SignInViewModel의 인스턴스를 받습니다.
        viewModel = ViewModelProvider(this, viewModelFactory) [SignInViewModel::class.java]

        // Lifecycle.addObserver() 함수를 사용하여 AutoClearedDisposable 객체를 옵서버로 등록합니다.
        lifecycle += disposables
        // viewDisposables에서 이 엑티비티의 생명주기 이벤트를 받도록 합니다.
        lifecycle += viewDisposables

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

        // 액세스 토큰 이벤트를 구독합니다.
        viewDisposables += viewModel.accessToken
                // 액세스 토큰이 없는 경우는 무시합니다.
                .filter { !it.isEmpty }
                .observeOn(AndroidSchedulers.mainThread())
                // 액세스 토큰이 있는 것을 확인했다면 메인 화면으로 이동합니다.
                .subscribe { launchMainActivity() }

        // 에러 메시지 이벤트를 구독합니다.
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message -> showError(message) }

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

        // 기기에 저장되어 있는 액세스 토큰을 불러옵니다.
        disposables += viewModel.loadAccessToken()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        showProgress()

        val uri = intent.data ?: throw IllegalArgumentException("No data exists")
        val code = uri.getQueryParameter("code") ?: throw IllegalStateException("No code exists")

        getAccessToken(code)
    }

    /**
     * lifecycle 사용으로 onStop() 함수는 더 이상 오버라이드 하지 않아도 됩니다.
     */
//    override fun onStop() {
//        super.onStop()
//        // 관리하고 있던 디스포저블 객체를 모두 해제합니다.
////        accessTokenCall?.run { cancel() } 대신 사용합니다.
//        disposables.clear()
//    }

    private fun getAccessToken(code: String) {
        // ViewModel에 정의된 함수를 사용하여 새로운 액세스 토큰을 요청합니다.
        disposables += viewModel.requestAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
    }

    private fun showProgress() {
        binding.btnActivitySignInStart.visibility = View.GONE
        binding.pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.btnActivitySignInStart.visibility = View.VISIBLE
        binding.pbActivitySignIn.visibility = View.GONE
    }

    private fun showError(message: String) {
        longToast(message)
    }

    private fun launchMainActivity() {
        startActivity(intentFor<MainActivity>().clearTask().newTask())
    }
}