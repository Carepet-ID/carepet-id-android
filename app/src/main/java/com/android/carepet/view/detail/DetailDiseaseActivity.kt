package com.android.carepet.view.detail

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.android.carepet.R
import com.android.carepet.dashboard.DashboardActivity
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.pref.UserPreference
import com.android.carepet.data.pref.UserRepository
import com.android.carepet.data.response.DiseaseResponse
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.*

class DetailDiseaseActivity : AppCompatActivity() {
    private var currentImageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var resultTextView: TextView
    private lateinit var diseaseNameTextView: TextView
    private lateinit var accuracyTextView: TextView
    private lateinit var diseaseIdTextView: TextView
    private lateinit var resultImageView: ImageView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_disease)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Care Pet"

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("openHomeFragment", true)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageView: ImageView = findViewById(R.id.previewImageView)
        val imageUriString = intent.getStringExtra("IMAGE_URI")
        imageUriString?.let {
            val imageUri = Uri.parse(it)
            currentImageUri = imageUri
            Glide.with(this)
                .load(imageUri)
                .into(imageView)
        }

        progressBar = findViewById(R.id.progressBar)
        resultTextView = findViewById(R.id.resultTextView)
        diseaseNameTextView = findViewById(R.id.diseaseNameTextView)
        accuracyTextView = findViewById(R.id.accuracyTextView)
        diseaseIdTextView = findViewById(R.id.diseaseIdTextView)
        resultImageView = findViewById(R.id.resultImageView)

        userRepository = UserRepository.getInstance(
            ApiConfig.getApiService(this),
            UserPreference.getInstance(this).getDataStore(),
            getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        )

        uploadImage()
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image Classification File", "showImage: ${imageFile.path}")
            showLoading(true)

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            Log.d("DetailDiseaseActivity", "multipartBody: ${multipartBody.body.contentType()}, ${multipartBody.body.contentLength()} bytes")

            lifecycleScope.launch {
                val token = userRepository.getTokenFromPreferences()

                Log.d("DetailDiseaseActivity", "Token: $token")

                if (token.isNullOrEmpty()) {
                    showToast("Token not found. Please login again.")
                    showLoading(false)
                    return@launch
                }

                try {
                    val apiService = ApiConfig.getApiService(this@DetailDiseaseActivity)
                    val response = apiService.predict(
                        multipartBody,
                        "Bearer $token"
                    )

                    if (response.status == "success" && response.predict != null) {
                        val predict = response.predict
                        diseaseNameTextView.text = predict.name
                        accuracyTextView.text = "Accuracy: ${predict.accuracy}%"
                        diseaseIdTextView.text = "Disease ID: ${predict.diseaseId}"
                        Glide.with(this@DetailDiseaseActivity)
                            .load(predict.photo)
                            .into(resultImageView)
                        fetchDiseaseDetails(predict.diseaseId)
                    } else {
                        showToast("Prediction failed: ${response.message}")
                        showLoading(false)
                    }
                } catch (e: HttpException) {
                    Log.e("DetailDiseaseActivity", "HttpException: ${e.message()}")
                    showToast("Error uploading image: ${e.message()}")
                    showLoading(false)
                } catch (e: Exception) {
                    Log.e("DetailDiseaseActivity", "Exception: ${e.message}")
                    showToast("Error uploading image: ${e.message}")
                    showLoading(false)
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun fetchDiseaseDetails(diseaseId: String) {
        lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getApiService(this@DetailDiseaseActivity)
                val response = apiService.getDiseaseById(diseaseId)

                displayDiseaseDetails(response)
            } catch (e: HttpException) {
                Log.e("DetailDiseaseActivity", "HttpException: ${e.message()}")
                showToast("Error fetching disease details: ${e.message()}")
            } catch (e: Exception) {
                Log.e("DetailDiseaseActivity", "Exception: ${e.message}")
                showToast("Error fetching disease details: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayDiseaseDetails(disease: DiseaseResponse) {
        val details = """
            Name: ${disease.name}
            Category: ${disease.category}
            Description: ${disease.description}
            Symptoms: ${disease.symptoms}
            Treatment: ${disease.treatment}
        """.trimIndent()
        resultTextView.text = details
    }

    private fun uriToFile(selectedImageUri: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val file = createTempFile(context)
        val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)
        val outputStream: OutputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun File.reduceFileImage(): File {
        val maximalSIZE = 1000000
        val bitmap = BitmapFactory.decodeFile(this.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > maximalSIZE)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(this))
        return this
    }

    private fun createTempFile(context: Context): File {
        val fileName = "temp_image"
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile(fileName, ".jpg", storageDir)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) {
            ProgressBar.VISIBLE
        } else {
            ProgressBar.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
