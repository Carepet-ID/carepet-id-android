package com.android.carepet.dashboard.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.carepet.R
import com.android.carepet.view.ViewModelFactory
import com.android.carepet.view.login.LoginViewModel
import com.bumptech.glide.Glide

class AccountFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_account, container, false)

        // Initialize ViewModel with custom ViewModelFactory
        val factory = ViewModelFactory.getInstance(requireContext())
        loginViewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)

        val userPhoto: ImageView = rootView.findViewById(R.id.userPhoto)
        val username: TextView = rootView.findViewById(R.id.username)
        val email: TextView = rootView.findViewById(R.id.email)
        val role: TextView = rootView.findViewById(R.id.role)

        // Observe the profile detail data from the ViewModel
        loginViewModel.profileDetail.observe(viewLifecycleOwner, Observer { profile ->
            profile?.let {
                username.text = it.username
                email.text = it.email
                role.text = it.role
                // Check if photo URL is empty or null
                if (it.photo.isNullOrEmpty()) {
                    userPhoto.setImageResource(R.drawable.ic_image_placeholder)
                } else {
                    Glide.with(this).load(it.photo).into(userPhoto)
                }
            }
        })

        // Fetch the profile details when the view is created
        loginViewModel.fetchProfileDetail()

        return rootView
    }
}