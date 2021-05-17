package com.androidhuman.example.simplegithub.ui.search;

import com.androidhuman.example.simplegithub.R;
import com.androidhuman.example.simplegithub.api.GithubApi;
import com.androidhuman.example.simplegithub.api.GithubApiProvider;
import com.androidhuman.example.simplegithub.api.model.GithubRepo;
import com.androidhuman.example.simplegithub.api.model.RepoSearchResponse;
import com.androidhuman.example.simplegithub.databinding.ActivitySearchBinding;
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.ItemClickListener {

    private ActivitySearchBinding binding;

    MenuItem menuSearch;
    SearchView searchView;

    SearchAdapter adapter;

    GithubApi api;
    Call<RepoSearchResponse> searchCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_search);

        adapter = new SearchAdapter();
        adapter.setItemClickListener(this);
        binding.rvActivitySearchList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvActivitySearchList.setAdapter(adapter);

        api = GithubApiProvider.provideGithubApi(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_search, menu);
        menuSearch = menu.findItem(R.id.menu_activity_search_query);

        searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateTitle(query);
                hideSoftKeyboard();
                collapseSearchView();
                searchRepository(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        menuSearch.expandActionView();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_activity_search_query == item.getItemId()) {
            item.expandActionView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(GithubRepo repository) {
        Intent intent = new Intent(this, RepositoryActivity.class);
        intent.putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login);
        intent.putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name);
        startActivity(intent);
    }

    private void searchRepository(String query) {
        clearResults();
        hideError();
        showProgress();

        searchCall = api.searchRepository(query);
        searchCall.enqueue(new Callback<RepoSearchResponse>() {
            @Override
            public void onResponse(Call<RepoSearchResponse> call,
                    Response<RepoSearchResponse> response) {
                hideProgress();

                RepoSearchResponse searchResult = response.body();
                if (response.isSuccessful() && null != searchResult) {
                    adapter.setItems(searchResult.items);
                    adapter.notifyDataSetChanged();

                    if (0 == searchResult.totalCount) {
                        showError(getString(R.string.no_search_result));
                    }
                } else {
                    showError("Not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RepoSearchResponse> call, Throwable t) {
                hideProgress();
                showError(t.getMessage());
            }
        });
    }

    private void updateTitle(String query) {
        ActionBar ab = getSupportActionBar();
        if (null != ab) {
            ab.setSubtitle(query);
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    private void collapseSearchView() {
        menuSearch.collapseActionView();
    }

    private void clearResults() {
        adapter.clearItems();
        adapter.notifyDataSetChanged();
    }

    private void showProgress() {
        binding.pbActivitySearch.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        binding.pbActivitySearch.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.tvActivitySearchMessage.setText(message);
        binding.tvActivitySearchMessage.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvActivitySearchMessage.setText("");
        binding.tvActivitySearchMessage.setVisibility(View.GONE);
    }
}
