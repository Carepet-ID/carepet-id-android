package com.android.carepet.view.dogs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.carepet.R
import com.android.carepet.dashboard.fragment.DogsFragment
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.api.ApiService
import com.android.carepet.data.pref.UserPreference
import com.android.carepet.data.response.DogResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class AddDogsActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var photoUri: Uri
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageViewSelectedPhoto: ImageView
    private lateinit var genderDropdown: Spinner

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    private fun hasReadExternalStoragePermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dogs)

        apiService = ApiConfig.getApiService(this)

        val editTextDogName: EditText = findViewById(R.id.editTextDogName)
        val editTextDogAge: EditText = findViewById(R.id.editTextDogAge)
        val editTextDogBreed: EditText = findViewById(R.id.editTextDogBreed)
        val editTextDogSkinColor: EditText = findViewById(R.id.editTextDogSkinColor)
        val editTextDogBirthday: EditText = findViewById(R.id.editTextDogBirthday)
        val editTextDogAbout: EditText = findViewById(R.id.editTextDogAbout)
        val buttonAddDog: Button = findViewById(R.id.buttonAddDog)
        val buttonSelectPhoto: Button = findViewById(R.id.buttonSelectPhoto)
        imageViewSelectedPhoto = findViewById(R.id.imageViewSelectedPhoto)
        genderDropdown = findViewById(R.id.spinnerDogGender)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.orange)
        }

        val editTexts = listOf(
            editTextDogName, editTextDogAge, editTextDogBreed, editTextDogSkinColor, editTextDogBirthday, editTextDogAbout
        )

        editTexts.forEach { editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()) {
                        editText.error = "This field is required"
                    } else {
                        editText.error = null
                    }
                }
            })
        }

        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            R.layout.spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderDropdown.adapter = genderAdapter

        buttonSelectPhoto.setOnClickListener {
            Log.d("AddDogsActivity", "Select Photo button clicked")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
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
            editTexts.forEach { editText ->
                if (editText.text.isNullOrEmpty()) {
                    editText.error = "This field is required"
                    allFieldsValid = false
                }
            }

            if (allFieldsValid && ::photoUri.isInitialized) {
                val name = editTextDogName.text.toString()
                val age = editTextDogAge.text.toString()
                val breed = editTextDogBreed.text.toString()
                val skinColor = editTextDogSkinColor.text.toString()
                val gender = genderDropdown.selectedItem.toString()
                val birthday = editTextDogBirthday.text.toString()
                val about = editTextDogAbout.text.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    val token = getAuthToken()
                    if (token.isNotEmpty()) {
                        addDog(name, age, breed, skinColor, gender, birthday, about, photoUri, token)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddDogsActivity, "Token is null or empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields and select a photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("AddDogsActivity", "Error opening gallery: ${e.message}")
            Toast.makeText(this, "Error opening gallery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            photoUri = data?.data ?: return
            imageViewSelectedPhoto.setImageURI(photoUri)
            Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addDog(name: String, age: String, breed: String, skinColor: String, gender: String, birthday: String, about: String, photoUri: Uri, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val photoPath = getRealPathFromURI(photoUri)
                val photoFile = File(photoPath)
                val photoRequestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody)

                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val agePart = age.toRequestBody("text/plain".toMediaTypeOrNull())
                val breedPart = breed.toRequestBody("text/plain".toMediaTypeOrNull())
                val skinColorPart = skinColor.toRequestBody("text/plain".toMediaTypeOrNull())
                val genderPart = gender.toRequestBody("text/plain".toMediaTypeOrNull())
                val birthdayPart = birthday.toRequestBody("text/plain".toMediaTypeOrNull())
                val aboutPart = about.toRequestBody("text/plain".toMediaTypeOrNull())

                val response: Response<DogResponse> = apiService.addNewDog(
                    "Bearer $token",
                    photoPart,
                    namePart,
                    aboutPart,
                    agePart,
                    birthdayPart,
                    breedPart,
                    skinColorPart,
                    genderPart
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showSuccessDialog()
                    } else {
                        Toast.makeText(this@AddDogsActivity, "Failed to add dog", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddDogsActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Add New Dog Success")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                navigateToDogsFragment()
            }
            .show()
    }

    private fun navigateToDogsFragment() {
        val intent = Intent(this, DogsFragment::class.java)
        intent.putExtra("navigateTo", "DogsFragment")
        startActivity(intent)
        finish()
    }

    private suspend fun getAuthToken(): String {
        val userPreference = UserPreference.getInstance(applicationContext)
        val user = userPreference.getSession().firstOrNull()
        return user?.token ?: ""
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            cursor?.getString(columnIndex ?: 0) ?: ""
        } finally {
            cursor?.close()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}
