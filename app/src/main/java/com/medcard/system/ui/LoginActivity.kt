package com.medcard.system.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.medcard.system.R
import com.medcard.system.security.BiometricAuthManager
import com.medcard.system.viewmodel.AuthViewModel

/**
 * Активность входа в систему с многофакторной аутентификацией.
 * После успешного входа данные врача загружаются из Firestore,
 * проверяется статус аккаунта (isActive), и только затем
 * происходит переход в главный экран.
 */
class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var biometricAuthManager: BiometricAuthManager

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var biometricButton: MaterialButton
    private lateinit var registerButton: MaterialButton

    // Временное хранение credentials для сохранения после успешного входа
    private var pendingEmail: String? = null
    private var pendingPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        biometricAuthManager = BiometricAuthManager(this)

        initViews()
        setupObservers()
        setupListeners()

        if (biometricAuthManager.isBiometricAvailable()) {
            biometricButton.isEnabled = true
        }
    }

    private fun initViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        biometricButton = findViewById(R.id.biometricButton)
        registerButton = findViewById(R.id.registerButton)
    }

    private fun setupObservers() {
        // Навигация только после загрузки данных пользователя из Firestore
        viewModel.userData.observe(this) { user ->
            if (user == null) return@observe

            if (!user.isActive) {
                viewModel.logout()
                pendingEmail = null
                pendingPassword = null
                Toast.makeText(
                    this,
                    "Аккаунт деактивирован. Обратитесь к администратору.",
                    Toast.LENGTH_LONG
                ).show()
                return@observe
            }

            // Сохраняем credentials для биометрии после успешного входа
            pendingEmail?.let { email ->
                pendingPassword?.let { password ->
                    saveEncryptedCredentials(email, password)
                }
            }
            pendingEmail = null
            pendingPassword = null

            navigateToMain()
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            loginButton.isEnabled = !isLoading
            biometricButton.isEnabled = !isLoading && biometricAuthManager.isBiometricAvailable()
            registerButton.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (validateInput(email, password)) {
                pendingEmail = email
                pendingPassword = password
                viewModel.login(email, password)
            }
        }

        biometricButton.setOnClickListener {
            authenticateWithBiometric()
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailInput.error = "Введите email"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Неверный формат email"
            return false
        }
        if (password.isEmpty()) {
            passwordInput.error = "Введите пароль"
            return false
        }
        if (password.length < 6) {
            passwordInput.error = "Пароль должен содержать минимум 6 символов"
            return false
        }
        return true
    }

    private fun authenticateWithBiometric() {
        biometricAuthManager.authenticate(
            activity = this,
            title = "Вход в МедКарта",
            subtitle = "Используйте биометрию для входа в систему",
            onSuccess = {
                loadEncryptedCredentials()
            },
            onError = { error ->
                Toast.makeText(this, "Ошибка: $error", Toast.LENGTH_SHORT).show()
            },
            onFailed = {
                Toast.makeText(this, "Аутентификация не пройдена", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Сохраняет email и пароль в зашифрованном хранилище (AES256-GCM).
     */
    private fun saveEncryptedCredentials(email: String, password: String) {
        try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val prefs = EncryptedSharedPreferences.create(
                this,
                "auth_prefs_encrypted",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit()
                .putString("email", email)
                .putString("password", password)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Загружает credentials из зашифрованного хранилища и выполняет вход.
     */
    private fun loadEncryptedCredentials() {
        try {
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val prefs = EncryptedSharedPreferences.create(
                this,
                "auth_prefs_encrypted",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val savedEmail = prefs.getString("email", null)
            val savedPassword = prefs.getString("password", null)

            if (savedEmail != null && savedPassword != null) {
                viewModel.login(savedEmail, savedPassword)
            } else {
                Toast.makeText(this, "Сохраненные учетные данные не найдены. Войдите с паролем.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка чтения учётных данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}