package com.android.carepet.dashboard.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.carepet.R
import com.android.carepet.view.ViewModelFactory
import com.android.carepet.view.login.LoginViewModel

class AccountFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_account, container, false)

        val factory = ViewModelFactory.getInstance(requireContext())
        loginViewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)

        val username: TextView = rootView.findViewById(R.id.username)
        val email: TextView = rootView.findViewById(R.id.email)
        val role: TextView = rootView.findViewById(R.id.role)

        loginViewModel.profileDetail.observe(viewLifecycleOwner, Observer { profile ->
            profile?.let {
                username.text = it.username
                email.text = it.email
                role.text = it.role
            }
        })

        loginViewModel.fetchProfileDetail()

        return rootView
    }
}