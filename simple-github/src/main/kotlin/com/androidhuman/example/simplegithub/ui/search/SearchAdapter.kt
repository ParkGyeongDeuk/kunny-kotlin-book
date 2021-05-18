package com.androidhuman.example.simplegithub.ui.search

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.databinding.ItemRepositoryBinding
import com.androidhuman.example.simplegithub.ui.GlideApp
import java.util.*

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.RepositoryHolder>() {

    private var items: MutableList<GithubRepo> = ArrayList()
    private val placeholder = ColorDrawable(Color.GRAY)
    private var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryHolder {
        return RepositoryHolder(ItemRepositoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RepositoryHolder, position: Int) {
        val repo = items[position]

        with(holder.binding) {
            GlideApp.with(root.context)
                    .load(repo.owner.avatarUrl)
                    .placeholder(placeholder)
                    .into(ivItemRepositoryProfile)

            tvItemRepositoryName.text = repo.fullName
            tvItemRepositoryLanguage.text = if (TextUtils.isEmpty(repo.language))
                root.context.getText(R.string.no_language_specified)
            else
                repo.language

            root.setOnClickListener {
                if (null != listener) {
                    listener!!.onItemClick(repo)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: List<GithubRepo>) {
        this.items = items.toMutableList()
    }

    fun setItemClickListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    fun clearItems() {
        items.clear()
    }

    inner class RepositoryHolder(val binding: ItemRepositoryBinding) : RecyclerView.ViewHolder(binding.root)

    interface ItemClickListener {
        fun onItemClick(repository: GithubRepo)
    }
}