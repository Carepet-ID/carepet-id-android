package com.android.carepet.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.carepet.R
import com.android.carepet.dashboard.DashboardActivity
import com.android.carepet.databinding.ActivityMainBinding
import com.android.carepet.view.ViewModelFactory
import com.android.carepet.view.login.LoginActivity
import com.android.carepet.view.login.LoginViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this)
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        loginViewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.orange)
        }

        setupAction()
    }

    private fun setupAction() {
        binding.btnLetStart.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
