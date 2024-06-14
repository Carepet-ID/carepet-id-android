package com.android.carepet.dashboard.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.carepet.R
import com.android.carepet.data.response.Article
import com.android.carepet.databinding.ItemArticleBinding
import com.android.carepet.view.article.ArticleDetailActivity
import com.bumptech.glide.Glide
import org.json.JSONObject

class ArticleAdapter(private val context: Context) : PagingDataAdapter<Article, ArticleAdapter.ArticleViewHolder>(ARTICLE_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position)
        holder.bind(article)
    }

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article?) {
            binding.article = article
            article?.let {
                Glide.with(binding.imageView.context)
                    .load(it.photo)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.imageView)

                updateBookmarkIcon(binding, it)

                binding.root.setOnClickListener { view ->
                    val context = view.context
                    val intent = Intent(context, ArticleDetailActivity::class.java).apply {
                        putExtra("article_id", it.id)
                        putExtra("article_title", it.title)
                        putExtra("article_content", it.content)
                        putExtra("article_category", it.category)
                        putExtra("article_author", it.author)
                        putExtra("article_createdAt", it.createdAt)
                        putExtra("article_photo", it.photo)
                    }
                    context.startActivity(intent)
                }

                binding.bookmarkIcon.setOnClickListener {
                    handleBookmarkClick(it, binding)
                }
            }
            binding.executePendingBindings()
        }

        private fun updateBookmarkIcon(binding: ItemArticleBinding, article: Article) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
            val isBookmarked = sharedPreferences.contains(article.id)
            val bookmarkIconRes = if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.bookmark_multiple_outline
            binding.bookmarkIcon.setImageResource(bookmarkIconRes)
        }

        private fun handleBookmarkClick(view: View, binding: ItemArticleBinding) {
            val article = binding.article ?: return
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
            val isBookmarked = sharedPreferences.contains(article.id)

            if (isBookmarked) {
                sharedPreferences.edit {
                    remove(article.id)
                }
                binding.bookmarkIcon.setImageResource(R.drawable.bookmark_multiple_outline)
            } else {
                val articleJson = JSONObject().apply {
                    put("title", article.title)
                    put("content", article.content)
                    put("category", article.category)
                    put("author", article.author)
                    put("createdAt", article.createdAt)
                    put("photo", article.photo)
                }
                sharedPreferences.edit {
                    putString(article.id, articleJson.toString())
                }
                binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmarked)
            }
        }
    }

    companion object {
        private val ARTICLE_COMPARATOR = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }
    }
}
