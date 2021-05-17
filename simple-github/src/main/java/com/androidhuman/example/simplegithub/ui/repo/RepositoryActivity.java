package com.androidhuman.example.simplegithub.ui.repo;

import com.androidhuman.example.simplegithub.R;
import com.androidhuman.example.simplegithub.api.GithubApi;
import com.androidhuman.example.simplegithub.api.GithubApiProvider;
import com.androidhuman.example.simplegithub.api.model.GithubRepo;
import com.androidhuman.example.simplegithub.databinding.ActivityRepositoryBinding;
import com.androidhuman.example.simplegithub.ui.GlideApp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepositoryActivity extends AppCompatActivity {

    public static final String KEY_USER_LOGIN = "user_login";
    public static final String KEY_REPO_NAME = "repo_name";

    private ActivityRepositoryBinding binding;

    GithubApi api;
    Call<GithubRepo> repoCall;

    SimpleDateFormat dateFormatInResponse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
    SimpleDateFormat dateFormatToShow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepositoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        api = GithubApiProvider.provideGithubApi(this);

        String login = getIntent().getStringExtra(KEY_USER_LOGIN);
        if (null == login) {
            throw new IllegalArgumentException("No login info exists in extras");
        }
        String repo = getIntent().getStringExtra(KEY_REPO_NAME);
        if (null == repo) {
            throw new IllegalArgumentException("No repo info exists in extras");
        }

        showRepositoryInfo(login, repo);
    }

    private void showRepositoryInfo(String login, String repoName) {
        showProgress();

        repoCall = api.getRepository(login, repoName);
        repoCall.enqueue(new Callback<GithubRepo>() {
            @Override
            public void onResponse(Call<GithubRepo> call, Response<GithubRepo> response) {
                hideProgress(true);

                GithubRepo repo = response.body();
                if (response.isSuccessful() && null != repo) {
                    GlideApp.with(RepositoryActivity.this)
                            .load(repo.owner.avatarUrl)
                            .into(binding.ivActivityRepositoryProfile);

                    binding.tvActivityRepositoryName.setText(repo.fullName);
                    binding.tvActivityRepositoryStars.setText(getResources()
                            .getQuantityString(R.plurals.star, repo.stars, repo.stars));
                    if (null == repo.description) {
                        binding.tvActivityRepositoryDescription.setText(R.string.no_description_provided);
                    } else {
                        binding.tvActivityRepositoryDescription.setText(repo.description);
                    }
                    if (null == repo.language) {
                        binding.tvActivityRepositoryLanguage.setText(R.string.no_language_specified);
                    } else {
                        binding.tvActivityRepositoryLanguage.setText(repo.language);
                    }

                    try {
                        Date lastUpdate = dateFormatInResponse.parse(repo.updatedAt);
                        binding.tvActivityRepositoryLastUpdate.setText(dateFormatToShow.format(lastUpdate));
                    } catch (ParseException e) {
                        binding.tvActivityRepositoryLastUpdate.setText(getString(R.string.unknown));
                    }
                } else {
                    showError("Not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GithubRepo> call, Throwable t) {
                hideProgress(false);
                showError(t.getMessage());
            }
        });
    }

    private void showProgress() {
        binding.clActivityRepositoryContent.setVisibility(View.GONE);
        binding.pbActivityRepository.setVisibility(View.VISIBLE);
    }

    private void hideProgress(boolean isSucceed) {
        binding.clActivityRepositoryContent.setVisibility(isSucceed ? View.VISIBLE : View.GONE);
        binding.pbActivityRepository.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.tvActivityRepositoryMessage.setText(message);
        binding.tvActivityRepositoryMessage.setVisibility(View.VISIBLE);
    }
}
