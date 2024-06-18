package com.android.carepet.view.dogs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.carepet.R
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.api.ApiService
import com.android.carepet.data.pref.UserPreference
import com.android.carepet.data.response.DogResponse
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class EditDogsActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var photoUri: Uri
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageViewSelectedPhoto: ImageView
    private var dogId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dogs)

        apiService = ApiConfig.getApiService(this)

        val editTextDogName: EditText = findViewById(R.id.editTextDogName)
        val editTextDogAge: EditText = findViewById(R.id.editTextDogAge)
        val editTextDogBreed: EditText = findViewById(R.id.editTextDogBreed)
        val editTextDogSkinColor: EditText = findViewById(R.id.editTextDogSkinColor)
        val editTextDogGender: EditText = findViewById(R.id.editTextDogGender)
        val editTextDogBirthday: EditText = findViewById(R.id.editTextDogBirthday)
        val editTextDogAbout: EditText = findViewById(R.id.editTextDogAbout)
        val buttonAddDog: Button = findViewById(R.id.buttonAddDog)
        val buttonSelectPhoto: Button = findViewById(R.id.buttonSelectPhoto)
        imageViewSelectedPhoto = findViewById(R.id.imageViewSelectedPhoto)

        // Get dog data from intent
        val dog: DogResponse? = intent.getParcelableExtra("dog")
        dog?.let {
            dogId = it.id
            editTextDogName.setText(it.name)
            editTextDogAge.setText(it.age.toString())
            editTextDogBreed.setText(it.breed)
            editTextDogSkinColor.setText(it.skinColor)
            editTextDogGender.setText(it.gender)
            editTextDogBirthday.setText(it.birthday)
            editTextDogAbout.setText(it.about)
            Glide.with(this).load(it.photo).into(imageViewSelectedPhoto)
        }

        buttonAddDog.text = "Update Dog"

        buttonSelectPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                openGallery()
            }
        }

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                photoUri = data?.data ?: return@registerForActivityResult
                imageViewSelectedPhoto.setImageURI(photoUri)
                Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
            }
        }

        buttonAddDog.setOnClickListener {
            var allFieldsValid = true
            val editTexts = listOf(
                editTextDogName, editTextDogAge, editTextDogBreed, editTextDogSkinColor,
                editTextDogGender, editTextDogBirthday, editTextDogAbout
            )
            editTexts.forEach { editText ->
                if (editText.text.isNullOrEmpty()) {
                    editText.error = "This field is required"
                    allFieldsValid = false
                }
            }

            if (allFieldsValid) {
                val name = editTextDogName.text.toString()
                val age = editTextDogAge.text.toString()
                val breed = editTextDogBreed.text.toString()
                val skinColor = editTextDogSkinColor.text.toString()
                val gender = editTextDogGender.text.toString()
                val birthday = editTextDogBirthday.text.toString()
                val about = editTextDogAbout.text.toString()

                Log.d("EditDogsActivity", "name: $name, age: $age, breed: $breed, skinColor: $skinColor, gender: $gender, birthday: $birthday, about: $about")

                CoroutineScope(Dispatchers.IO).launch {
                    val token = getAuthToken()
                    if (token.isNotEmpty()) {
                        updateDog(name, age, breed, skinColor, gender, birthday, about, token)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditDogsActivity, "Token is null or empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun updateDog(name: String, age: String, breed: String, skinColor: String, gender: String, birthday: String, about: String, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val photoPart: MultipartBody.Part? = if (::photoUri.isInitialized) {
                    val photoPath = getRealPathFromURI(photoUri)
                    val photoFile = File(photoPath)
                    val photoRequestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody)
                } else {
                    null
                }

                // Logging input parameters to debug
                Log.d("UpdateDog", "name: $name, age: $age, breed: $breed, gender: $gender, birthday: $birthday, about: $about")

                // Creating RequestBody objects for each input field
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val agePart = age.toRequestBody("text/plain".toMediaTypeOrNull())
                val breedPart = breed.toRequestBody("text/plain".toMediaTypeOrNull())
                val genderPart = gender.toRequestBody("text/plain".toMediaTypeOrNull())
                val birthdayPart = birthday.toRequestBody("text/plain".toMediaTypeOrNull())
                val aboutPart = about.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.updateDogDetail(
                    "Bearer $token",
                    dogId!!,
                    namePart,
                    genderPart,
                    birthdayPart,
                    agePart,
                    breedPart,
                    aboutPart,
                    photoPart
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showSuccessDialog()
                        setResult(Activity.RESULT_OK) // Notify DogsFragment of the update
                    } else {
                        Toast.makeText(this@EditDogsActivity, "Failed to update dog", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditDogsActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Update Dog Success")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                finish() // Close the activity
            }
            .show()
    }

    private suspend fun getAuthToken(): String {
        val userPreference = UserPreference.getInstance(applicationContext)
        val user = userPreference.getSession().firstOrNull()
        return user?.token ?: ""
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val cursor = contentResolver.query(contentUri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = cursor?.getString(columnIndex ?: 0) ?: ""
        cursor?.close()
        return path
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }
}
