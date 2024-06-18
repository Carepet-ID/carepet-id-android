package com.android.carepet.dashboard.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.carepet.R
import com.android.carepet.data.api.ApiConfig
import com.android.carepet.data.api.ApiService
import com.android.carepet.data.pref.UserPreference
import com.android.carepet.data.response.DogResponse
import com.android.carepet.view.dogs.AddDogsActivity
import com.android.carepet.view.dogs.DogsAdapter
import com.android.carepet.view.dogs.EditDogsActivity
import com.android.carepet.view.dogs.detail.DogsDetailActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DogsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var dogsAdapter: DogsAdapter
    private lateinit var apiService: ApiService
    private lateinit var buttonAddDog: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dogs, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewDogs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        dogsAdapter = DogsAdapter(
            { dog -> showDeleteConfirmationDialog(dog) },
            { dog -> navigateToEditDog(dog) },
            { dog -> navigateToDogDetail(dog) }
        )
        recyclerView.adapter = dogsAdapter

        buttonAddDog = view.findViewById(R.id.buttonAddDog)
        buttonAddDog.setOnClickListener {
            val intent = Intent(requireContext(), AddDogsActivity::class.java)
            startActivity(intent)
        }

        apiService = ApiConfig.getApiService(requireContext())

        fetchDogs()

        return view
    }

    private fun fetchDogs() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getAuthToken()
                if (token.isNotEmpty()) {
                    val dogsList = apiService.getAllDogs(token)
                    withContext(Dispatchers.Main) {
                        dogsAdapter.submitList(dogsList)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Token is null or empty", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(dog: DogResponse) {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure?")
            .setPositiveButton("Confirm") { dialog, _ ->
                deleteDog(dog)
                dialog.dismiss()
                navigateToHomeFragment()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Toast.makeText(context, "Dog Deletion Cancelled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteDog(dog: DogResponse) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getAuthToken()
                if (token.isNotEmpty()) {
                    val response = apiService.deleteDog(token, dog.id)
                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            removeDogFromList(dog)
                            navigateToHomeFragment()
                        } else {
                            //Do Nothing
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Token is null or empty", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun removeDogFromList(dog: DogResponse) {
        val updatedList = dogsAdapter.getDogs().toMutableList()
        updatedList.remove(dog)
        dogsAdapter.submitList(updatedList)
    }

    private suspend fun getAuthToken(): String {
        val userPreference = UserPreference.getInstance(requireContext())
        val user = userPreference.getSession().firstOrNull()
        return user?.token ?: ""
    }

    private fun navigateToEditDog(dog: DogResponse) {
        val intent = Intent(requireContext(), EditDogsActivity::class.java)
        intent.putExtra("dog", dog)
        startActivityForResult(intent, EDIT_DOG_REQUEST_CODE)
    }

    private fun navigateToDogDetail(dog: DogResponse) {
        val intent = Intent(requireContext(), DogsDetailActivity::class.java)
        intent.putExtra("dog_id", dog.id)
        startActivity(intent)
    }

    private fun navigateToHomeFragment() {
        val activity = requireActivity()
        val fragmentTransaction = activity.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, HomeFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.home
    }

    override fun onResume() {
        super.onResume()
        fetchDogs()
    }

    companion object {
        private const val EDIT_DOG_REQUEST_CODE = 1
    }
}