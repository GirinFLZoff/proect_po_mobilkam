package com.medcard.system.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.medcard.system.model.User
import com.medcard.system.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для аутентификации пользователей
 */
class AuthViewModel : ViewModel() {
    
    private val repository = AuthRepository()
    
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser
    
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        checkCurrentUser()
    }
    
    /**
     * Проверка текущего пользователя
     */
    private fun checkCurrentUser() {
        _currentUser.value = repository.getCurrentUser()
        
        _currentUser.value?.let { user ->
            loadUserData(user.uid)
        }
    }
    
    /**
     * Вход пользователя
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.login(email, password)
            
            result.onSuccess { user ->
                _currentUser.value = user
                loadUserData(user.uid)
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _loading.value = false
        }
    }
    
    /**
     * Регистрация нового пользователя
     */
    fun register(email: String, password: String, userData: User) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.register(email, password, userData)
            
            result.onSuccess { user ->
                _currentUser.value = user
                loadUserData(user.uid)
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _loading.value = false
        }
    }
    
    /**
     * Загрузка данных пользователя
     */
    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            val result = repository.getUserData(userId)
            
            result.onSuccess { user ->
                _userData.value = user
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }
    
    /**
     * Выход пользователя
     */
    fun logout() {
        repository.logout()
        _currentUser.value = null
        _userData.value = null
    }
    
    /**
     * Проверка авторизации
     */
    fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }
    
    /**
     * Сброс пароля
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.resetPassword(email)
            
            result.onSuccess {
                _error.value = "Ссылка для сброса пароля отправлена на email"
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _loading.value = false
        }
    }
    
    /**
     * Очистка ошибки
     */
    fun clearError() {
        _error.value = null
    }
}
