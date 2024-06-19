package com.android.carepet.view.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.carepet.data.pref.UserRepository
import com.android.carepet.data.response.Result
import kotlinx.coroutines.launch
import android.util.Patterns

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<Result<Any>>()
    val registerResult: LiveData<Result<Any>> = _registerResult

    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    fun register(username: String, email: String, password: String) {
        if (validateInput(username, email, password)) {
            viewModelScope.launch {
                _registerResult.value = Result.Loading
                try {
                    val response = repository.registerUser(username, email, password)
                    _registerResult.value = Result.Success(response)
                } catch (e: Exception) {
                    if (e.message == "username already exists") {
                        _usernameError.value = "Username already exists"
                    } else if (e.message == "email already exists") {
                        _emailError.value = "Email already exists"
                    } else {
                        _registerResult.value = Result.Error(e.message)
                    }
                }
            }
        }
    }

    fun validateUsername(username: String) {
        if (username.isEmpty()) {
            _usernameError.value = "Username cannot be empty"
        } else if (username.length < 5) {
            _usernameError.value = "Username must be at least 5 characters"
        } else {
            _usernameError.value = null
        }
    }

    fun validateEmail(email: String) {
        if (email.isEmpty()) {
            _emailError.value = "Email cannot be empty"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Invalid email format"
        } else {
            _emailError.value = null
        }
    }

    fun validatePassword(password: String) {
        if (password.isEmpty()) {
            _passwordError.value = "Password cannot be empty"
        } else if (password.length < 8) {
            _passwordError.value = "Password must be at least 8 characters"
        } else {
            _passwordError.value = null
        }
    }

    private fun validateInput(username: String, email: String, password: String): Boolean {
        var isValid = true
        if (username.isEmpty() || username.length < 5) {
            _usernameError.value = "Username must be at least 5 characters"
            isValid = false
        } else {
            _usernameError.value = null
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Invalid email format"
            isValid = false
        } else {
            _emailError.value = null
        }
        if (password.isEmpty() || password.length < 8) {
            _passwordError.value = "Password must be at least 8 characters"
            isValid = false
        } else {
            _passwordError.value = null
        }
        return isValid
    }
}
