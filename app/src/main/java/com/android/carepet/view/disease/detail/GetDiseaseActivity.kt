package com.android.carepet.view.disease.detail

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.android.carepet.R
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.response.DiseaseResponse
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class GetDiseaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_get_disease)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val diseaseId = intent.getStringExtra("disease_id") ?: return
        fetchDiseaseDetails(diseaseId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.orange)
        }
    }

    private fun fetchDiseaseDetails(diseaseId: String) {
        val apiService = ApiConfig.getApiService(this)
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences("my_prefs", MODE_PRIVATE).getString("auth_token", "") ?: ""
                val disease = apiService.getDiseaseById(diseaseId)
                displayDiseaseDetails(disease)
            } catch (e: Exception) {
                Toast.makeText(this@GetDiseaseActivity, "Failed to load disease details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayDiseaseDetails(disease: DiseaseResponse) {
        val imageView: ImageView = findViewById(R.id.diseaseImage)
        val titleView: TextView = findViewById(R.id.diseaseTitle)
        val categoryView: TextView = findViewById(R.id.diseaseCategory)
        val symptomsView: TextView = findViewById(R.id.diseaseSymptoms)
        val treatmentView: TextView = findViewById(R.id.diseaseTreatment)
        val descriptionView: TextView = findViewById(R.id.diseaseDescription)

        Glide.with(this).load(disease.photo).into(imageView)
        titleView.text = disease.name
        categoryView.text = disease.category
        symptomsView.text = disease.symptoms
        treatmentView.text = disease.treatment
        descriptionView.text = disease.description
    }
}
