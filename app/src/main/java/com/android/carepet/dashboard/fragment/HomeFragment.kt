package com.android.carepet.dashboard.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.carepet.R
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.response.Article
import com.android.carepet.view.article.ArticleDetailActivity
import com.android.carepet.view.disease.DiseaseActivity
import com.android.carepet.view.medicine.MedicineActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val scanSkin = view.findViewById<LinearLayout>(R.id.scanSkin)
        val showDisease = view.findViewById<LinearLayout>(R.id.showDisease)
        val showMedicine = view.findViewById<LinearLayout>(R.id.showMedicine)
        val btnMore = view.findViewById<TextView>(R.id.btnMore)

        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)

        btnMore.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace((view.parent as ViewGroup).id, ArticlesFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView.selectedItemId = R.id.articles
        }

        scanSkin.setOnClickListener {
            fab?.performClick()
        }

        showDisease.setOnClickListener {
            val intent = Intent(activity, DiseaseActivity::class.java)
            startActivity(intent)
        }

        showMedicine.setOnClickListener {
            val intent = Intent(activity, MedicineActivity::class.java)
            startActivity(intent)
        }

        fetchAndDisplayArticles(view)

        return view
    }

    private fun fetchAndDisplayArticles(view: View) {
        val sharedPreferences = requireContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", "") ?: ""

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiService = ApiConfig.getApiService(requireContext())
                val articles = apiService.getAllArticles("Bearer $token")

                withContext(Dispatchers.Main) {
                    displayArticles(view, articles.take(3))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the error appropriately
            }
        }
    }

    private fun displayArticles(view: View, articles: List<Article>) {
        val articlesContainer = view.findViewById<LinearLayout>(R.id.articlesContainer)

        articles.forEach { article ->
            val articleView = layoutInflater.inflate(R.layout.item_article_home, articlesContainer, false)

            val titleTextView = articleView.findViewById<TextView>(R.id.titleTextView)
            val contentTextView = articleView.findViewById<TextView>(R.id.contentTextView)
            val imageView = articleView.findViewById<ImageView>(R.id.imageView)

            titleTextView.text = article.title
            contentTextView.text = article.content
            Glide.with(this)
                .load(article.photo)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(imageView)

            articleView.setOnClickListener {
                val intent = Intent(activity, ArticleDetailActivity::class.java).apply {
                    putExtra("article_title", article.title)
                    putExtra("article_content", article.content)
                    putExtra("article_category", article.category)
                    putExtra("article_author", article.author)
                    putExtra("article_createdAt", article.createdAt)
                    putExtra("article_photo", article.photo)
                }
                startActivity(intent)
            }

            articlesContainer.addView(articleView)
        }
    }
}
