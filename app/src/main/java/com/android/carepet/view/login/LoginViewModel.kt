package com.android.carepet.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.carepet.data.pref.UserModel
import com.android.carepet.data.pref.UserRepository
import com.android.carepet.data.response.Result
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    fun login(username: String, password: String) {
        if (validateInput(username, password)) {
            viewModelScope.launch {
                _loginResult.value = Result.Loading
                try {
                    val response = repository.loginUser(username, password)
                    val role = response.request?.body?.formdata?.find { it.key == "role" }?.value ?: ""
                    val photo = response.request?.body?.formdata?.find { it.key == "photo" }?.src ?: ""
                    val token = response.request?.body?.formdata?.find { it.key == "token" }?.value

                    val user = UserModel(
                        username = username,
                        password = password,
                        role = role,
                        photo = photo,
                        token = token,
                        isLogin = true
                    )
                    repository.saveSession(user)
                    _loginResult.value = Result.Success(user)
                } catch (e: Exception) {
                    _loginResult.value = Result.Error(e.message)
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

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}
