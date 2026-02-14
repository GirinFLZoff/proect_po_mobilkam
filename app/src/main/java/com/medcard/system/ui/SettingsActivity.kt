package com.medcard.system.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.medcard.system.R
import com.medcard.system.model.UserRole
import com.medcard.system.security.BiometricAuthManager
import com.medcard.system.viewmodel.AuthViewModel

/**
 * Активность настроек приложения
 */
class SettingsActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var tvUserDepartment: TextView
    private lateinit var switchBiometric: SwitchMaterial
    private lateinit var btnChangePassword: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Настройки"

        initViews()
        setupObservers()
        setupListeners()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserRole = findViewById(R.id.tvUserRole)
        tvUserDepartment = findViewById(R.id.tvUserDepartment)
        switchBiometric = findViewById(R.id.switchBiometric)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        val biometricManager = BiometricAuthManager(this)
        switchBiometric.isEnabled = biometricManager.isBiometricAvailable()

        val prefs = getSharedPreferences("medcard_prefs", MODE_PRIVATE)
        switchBiometric.isChecked = prefs.getBoolean("biometric_enabled", false)
    }

    private fun setupObservers() {
        authViewModel.userData.observe(this) { user ->
            user?.let {
                tvUserName.text = it.getFullName().ifBlank { "Не указано" }
                tvUserEmail.text = it.email
                tvUserRole.text = "Роль: ${getRoleDisplayName(it.role)}"
                tvUserDepartment.text = "Отделение: ${it.department.ifBlank { "—" }}"
            }
        }
    }

    private fun setupListeners() {
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("medcard_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("biometric_enabled", isChecked).apply()
            val msg = if (isChecked) "Биометрический вход включен" else "Биометрический вход отключен"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun showChangePasswordDialog() {
        val email = authViewModel.currentUser.value?.email
        if (email != null) {
            authViewModel.resetPassword(email)
            Toast.makeText(this, "Ссылка для смены пароля отправлена на $email", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRoleDisplayName(role: UserRole): String {
        return when (role) {
            UserRole.ADMIN -> "Администратор"
            UserRole.DOCTOR -> "Врач"
            UserRole.NURSE -> "Медсестра"
            UserRole.REGISTRAR -> "Регистратор"
            UserRole.LAB_TECHNICIAN -> "Лаборант"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
