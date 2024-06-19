package com.android.carepet.view.dogs.detail

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.carepet.R
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.api.ApiService
import com.android.carepet.data.pref.UserPreference
import com.android.carepet.data.response.DogResponse
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DogsDetailActivity : AppCompatActivity() {

    private lateinit var imageViewDogPhoto: ImageView
    private lateinit var textViewDogName: TextView
    private lateinit var textViewDogGender: TextView
    private lateinit var textViewDogBreed: TextView
    private lateinit var textViewDogBirthday: TextView
    private lateinit var textViewDogAge: TextView
    private lateinit var textViewDogSkinColor: TextView
    private lateinit var textViewDogAbout: TextView
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dogs_detail)

        imageViewDogPhoto = findViewById(R.id.imageViewDogPhotoDetail)
        textViewDogName = findViewById(R.id.textViewDogNameDetail)
        textViewDogGender = findViewById(R.id.textViewDogGenderDetail)
        textViewDogBreed = findViewById(R.id.textViewDogBreedDetail)
        textViewDogBirthday = findViewById(R.id.textViewDogBirthdayDetail)
        textViewDogAge = findViewById(R.id.textViewDogAgeDetail)
        textViewDogSkinColor = findViewById(R.id.textViewDogSkinColorDetail)
        textViewDogAbout = findViewById(R.id.textViewDogAboutDetail)

        apiService = ApiConfig.getApiService(this)

        val dogId = intent.getStringExtra("dog_id")
        fetchDogDetails(dogId)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun fetchDogDetails(dogId: String?) {
        dogId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val token = getAuthToken()
                    if (token.isNotEmpty()) {
                        val dog = apiService.getDogDetail(token, dogId)
                        withContext(Dispatchers.Main) {
                            displayDogDetails(dog)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun displayDogDetails(dog: DogResponse) {
        Glide.with(this).load(dog.photo).into(imageViewDogPhoto)
        textViewDogName.text = dog.name
        textViewDogGender.text = dog.gender
        textViewDogBreed.text = dog.breed
        textViewDogBirthday.text = dog.birthday
        textViewDogAge.text = dog.age.toString()
        textViewDogSkinColor.text = dog.skinColor
        textViewDogAbout.text = dog.about
    }

    private suspend fun getAuthToken(): String {
        val userPreference = UserPreference.getInstance(this)
        val user = userPreference.getSession().firstOrNull()
        return user?.token ?: ""
    }
}
