package com.androidhuman.example.simplegithub.ui.search;

import com.androidhuman.example.simplegithub.R;
import com.androidhuman.example.simplegithub.api.model.GithubRepo;
import com.androidhuman.example.simplegithub.databinding.ItemRepositoryBinding;
import com.androidhuman.example.simplegithub.ui.GlideApp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.RepositoryHolder> {

    private List<GithubRepo> items = new ArrayList<>();

    private ColorDrawable placeholder = new ColorDrawable(Color.GRAY);

    @Nullable
    private ItemClickListener listener;

    @Override
    public RepositoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RepositoryHolder(ItemRepositoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(RepositoryHolder holder, int position) {
        final GithubRepo repo = items.get(position);

        GlideApp.with(holder.itemView.getContext())
                .load(repo.owner.avatarUrl)
                .placeholder(placeholder)
                .into(holder.binding.ivItemRepositoryProfile);

        holder.binding.tvItemRepositoryName.setText(repo.fullName);
        holder.binding.tvItemRepositoryLanguage.setText(TextUtils.isEmpty(repo.language)
                ? holder.itemView.getContext().getText(R.string.no_language_specified)
                : repo.language);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != listener) {
                    listener.onItemClick(repo);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(@NonNull List<GithubRepo> items) {
        this.items = items;
    }

    public void setItemClickListener(@Nullable ItemClickListener listener) {
        this.listener = listener;
    }

    public void clearItems() {
        this.items.clear();
    }

    static class RepositoryHolder extends RecyclerView.ViewHolder {

        private ItemRepositoryBinding binding;

        RepositoryHolder(ItemRepositoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface ItemClickListener {

        void onItemClick(GithubRepo repository);
    }
}