package com.android.carepet.view.article

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.carepet.R
import com.android.carepet.databinding.ActivityArticleDetailBinding
import com.bumptech.glide.Glide

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Article Detail"

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.orange)
        }

        val articleTitle = intent.getStringExtra("article_title")
        val articleContent = intent.getStringExtra("article_content")
        val articleCategory = intent.getStringExtra("article_category")
        val articleAuthor = intent.getStringExtra("article_author")
        val articleCreatedAt = intent.getStringExtra("article_createdAt")
        val articlePhoto = intent.getStringExtra("article_photo")

        binding.titleTextView.text = articleTitle
        binding.contentTextView.text = articleContent
        binding.categoryTextView.text = articleCategory
        binding.authorTextView.text = articleAuthor
        binding.createdAtTextView.text = articleCreatedAt
        Glide.with(this)
            .load(articlePhoto)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.imageView)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
