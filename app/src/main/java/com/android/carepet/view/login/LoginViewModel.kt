package com.android.carepet.view.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.carepet.data.pref.UserModel
import com.android.carepet.data.pref.UserRepository
import com.android.carepet.data.response.ProfileDetailResponse
import com.android.carepet.data.response.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _sessionSaved = MutableLiveData<Boolean>()
    val sessionSaved: LiveData<Boolean> = _sessionSaved

    private val _profileDetail = MutableLiveData<ProfileDetailResponse>()
    val profileDetail: LiveData<ProfileDetailResponse> = _profileDetail

    fun login(username: String, password: String) {
        if (validateInput(username, password)) {
            viewModelScope.launch {
                _loginResult.value = Result.Loading
                try {
                    val response = repository.loginUser(username, password)
                    // Token is now handled within the UserRepository and saved in UserModel

                    val user = repository.getSession().first() // Get the saved session
                    _loginResult.value = Result.Success(user)
                    _sessionSaved.value = true

                    Log.d("LoginViewModel", "Token after login: ${user.token}")
                } catch (e: Exception) {
                    _loginResult.value = Result.Error(e.message ?: "An unknown error occurred")
                }
            }
        }
    }

    fun validateUsername(username: String) {
        if (username.isEmpty()) {
            _usernameError.value = "Username cannot be empty"
        } else {
            _usernameError.value = null
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

    private fun validateInput(username: String, password: String): Boolean {
        var isValid = true
        if (username.isEmpty()) {
            _usernameError.value = "Username cannot be empty"
            isValid = false
        } else {
            _usernameError.value = null
        }
        if (password.isEmpty()) {
            _passwordError.value = "Password cannot be empty"
            isValid = false
        } else if (password.length < 8) {
            _passwordError.value = "Password must be at least 8 characters"
            isValid = false
        } else {
            _passwordError.value = null
        }
        return isValid
    }

    fun getToken(): String {
        val session = getSession().value
        return session?.token ?: ""
    }

    fun fetchProfileDetail() {
        viewModelScope.launch {
            try {
                val profile = repository.getProfileDetail()
                _profileDetail.value = profile
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error fetching profile detail: ${e.message}")
            }
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}
