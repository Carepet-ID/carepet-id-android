package com.android.carepet.dashboard.bookmark

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.carepet.R
import com.android.carepet.dashboard.adapter.ArticleAdapter
import com.android.carepet.data.response.Article
import com.android.carepet.databinding.ActivityBookmarkBinding
import kotlinx.coroutines.launch
import org.json.JSONObject

class BookmarkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookmarkBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val articleAdapter by lazy { ArticleAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("bookmarks", Context.MODE_PRIVATE)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BookmarkActivity)
            adapter = articleAdapter
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.orange)
        }

        loadBookmarkedArticles()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadBookmarkedArticles() {
        val bookmarks = sharedPreferences.all.map { (id, json) ->
            val articleJson = JSONObject(json.toString())
            Article(
                id = id,
                title = articleJson.optString("title", ""),
                content = articleJson.optString("content", ""),
                category = articleJson.optString("category", ""),
                author = articleJson.optString("author", ""),
                createdAt = articleJson.optString("createdAt", ""),
                photo = articleJson.optString("photo", "")
            )
        }

        if (bookmarks.isEmpty()) {
            binding.noArticlesTextView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.noArticlesTextView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            lifecycleScope.launch {
                articleAdapter.submitData(PagingData.from(bookmarks))
            }
        }
    }
}
