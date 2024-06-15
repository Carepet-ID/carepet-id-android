package com.android.carepet.view.disease

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.carepet.R
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.pref.UserPreference
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DiseaseActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var diseaseAdapter: DiseaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        diseaseAdapter = DiseaseAdapter()
        recyclerView.adapter = diseaseAdapter

        fetchDiseases()
    }

    private fun fetchDiseases() {
        val apiService = ApiConfig.getApiService(this)
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences("my_prefs", MODE_PRIVATE).getString("auth_token", "") ?: ""
                val response = apiService.getAllDiseases("Bearer $token")
                diseaseAdapter.submitList(response)
            } catch (e: Exception) {
                Toast.makeText(this@DiseaseActivity, "Failed to load diseases", Toast.LENGTH_SHORT).show()
            }
        }
    }
}