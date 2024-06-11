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
import com.android.carepet.data.response.FileUploadResponse
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class DetailDiseaseActivity : AppCompatActivity() {
    private var currentImageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var resultTextView: TextView

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

            val descriptionText = "Your description here"
            val description = descriptionText.toRequestBody("text/plain".toMediaType())

            lifecycleScope.launch {
                try {
                    val apiService = ApiConfig.getApiService(this@DetailDiseaseActivity)
                    val successResponse = apiService.uploadImage(multipartBody, description)
                    with(successResponse.data) {
                        resultTextView.text = if (isAboveThreshold == true) {
                            showToast(successResponse.message.toString())
                            String.format("%s with %.2f%%", result, confidenceScore)
                        } else {
                            showToast("Model is predicted successfully but under threshold.")
                            String.format("Please use the correct picture because the confidence score is %.2f%%", confidenceScore)
                        }
                    }
                    showLoading(false)
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
                    showToast(errorResponse.message.toString())
                    showLoading(false)
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
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
        val MAXIMAL_SIZE = 1000000
        val bitmap = BitmapFactory.decodeFile(this.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > MAXIMAL_SIZE)
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
