package com.android.carepet.view.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.android.carepet.R
import com.android.carepet.data.response.Result
import com.android.carepet.databinding.ActivityRegisterBinding
import com.android.carepet.view.ViewModelFactory
import com.android.carepet.view.login.LoginActivity
import com.android.carepet.view.setDrawableEndClickListener

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    private var isPasswordVisible = false

    private val registerViewModel: RegisterViewModel by viewModels { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            registerViewModel.register(username, email, password)
        }

        registerViewModel.registerResult.observe(this, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    progressBar.visibility = View.GONE
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Registration failed: ${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        registerViewModel.usernameError.observe(this, Observer { error ->
            etUsername.error = error
        })

        registerViewModel.emailError.observe(this, Observer { error ->
            etEmail.error = error
        })

        registerViewModel.passwordError.observe(this, Observer { error ->
            etPassword.error = error
        })

        etUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                registerViewModel.validateUsername(s.toString())
            }
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                registerViewModel.validateEmail(s.toString())
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                registerViewModel.validatePassword(s.toString())
            }
        })

        etPassword.setDrawableEndClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = null
                etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0)
            }
            etPassword.setSelection(etPassword.text.length)
        }
    }

    private fun setupAction() {
        binding.btnRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
