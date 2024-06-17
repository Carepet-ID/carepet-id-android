package com.android.carepet.view.medicine

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.carepet.R
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.pref.UserPreference
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MedicineActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        val toolbar: Toolbar = findViewById(R.id.toolbarBack)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.orange)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchProducts()
    }

    private fun fetchProducts() {
        lifecycleScope.launch {
            val userPreference = UserPreference.getInstance(applicationContext)
            val token = userPreference.getSession().firstOrNull()?.token ?: ""

            if (token.isEmpty()) {
                Toast.makeText(this@MedicineActivity, "Token is null or empty", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val apiService = ApiConfig.getApiService(applicationContext)
                val products = apiService.getAllProducts("Bearer $token")

                adapter = MedicineAdapter(products)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(this@MedicineActivity, "Failed to fetch products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}