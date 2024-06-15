package com.android.carepet.view.medicine

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.carepet.R
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.pref.UserPreference
import com.android.carepet.data.response.Product
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MedicineActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

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