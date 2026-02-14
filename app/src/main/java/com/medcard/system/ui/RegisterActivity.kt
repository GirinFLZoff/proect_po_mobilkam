package com.medcard.system.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.medcard.system.R
import com.medcard.system.model.Permission
import com.medcard.system.model.User
import com.medcard.system.model.UserRole
import com.medcard.system.viewmodel.AuthViewModel

/**
 * Активность регистрации нового врача.
 * Создаёт учётную запись в Firebase Authentication и сохраняет
 * профессиональные данные в коллекцию "users" в Firestore.
 */
class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var lastNameInput: TextInputEditText
    private lateinit var firstNameInput: TextInputEditText
    private lateinit var middleNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var positionInput: TextInputEditText
    private lateinit var departmentInput: TextInputEditText
    private lateinit var licenseNumberInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var backToLoginButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupObservers()
        setupListeners()
    }

    private fun initViews() {
        lastNameInput = findViewById(R.id.lastNameInput)
        firstNameInput = findViewById(R.id.firstNameInput)
        middleNameInput = findViewById(R.id.middleNameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        positionInput = findViewById(R.id.positionInput)
        departmentInput = findViewById(R.id.departmentInput)
        licenseNumberInput = findViewById(R.id.licenseNumberInput)
        phoneInput = findViewById(R.id.phoneInput)
        registerButton = findViewById(R.id.registerButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)
    }

    private fun setupObservers() {
        // Навигация происходит после того, как данные врача загружены из БД
        viewModel.userData.observe(this) { user ->
            if (user != null) {
                Toast.makeText(this, "Аккаунт создан. Добро пожаловать, ${user.getFullName()}!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            registerButton.isEnabled = !isLoading
            backToLoginButton.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            val lastName = lastNameInput.text.toString().trim()
            val firstName = firstNameInput.text.toString().trim()
            val middleName = middleNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            val position = positionInput.text.toString().trim()
            val department = departmentInput.text.toString().trim()
            val licenseNumber = licenseNumberInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            if (!validateInput(lastName, firstName, email, password, confirmPassword, position, department, licenseNumber)) {
                return@setOnClickListener
            }

            val doctorData = User(
                lastName = lastName,
                firstName = firstName,
                middleName = middleName,
                role = UserRole.DOCTOR,
                position = position,
                department = department,
                licenseNumber = licenseNumber,
                phone = phone,
                permissions = listOf(
                    Permission.READ_PATIENT,
                    Permission.WRITE_PATIENT,
                    Permission.READ_MEDICAL_RECORD,
                    Permission.WRITE_MEDICAL_RECORD,
                    Permission.PRESCRIBE_MEDICATION,
                    Permission.VIEW_LAB_RESULTS
                )
            )

            viewModel.register(email, password, doctorData)
        }

        backToLoginButton.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        lastName: String,
        firstName: String,
        email: String,
        password: String,
        confirmPassword: String,
        position: String,
        department: String,
        licenseNumber: String
    ): Boolean {
        if (lastName.isEmpty()) {
            lastNameInput.error = "Введите фамилию"
            lastNameInput.requestFocus()
            return false
        }
        if (firstName.isEmpty()) {
            firstNameInput.error = "Введите имя"
            firstNameInput.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            emailInput.error = "Введите email"
            emailInput.requestFocus()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Неверный формат email"
            emailInput.requestFocus()
            return false
        }
        if (password.length < 6) {
            passwordInput.error = "Пароль должен содержать минимум 6 символов"
            passwordInput.requestFocus()
            return false
        }
        if (password != confirmPassword) {
            confirmPasswordInput.error = "Пароли не совпадают"
            confirmPasswordInput.requestFocus()
            return false
        }
        if (position.isEmpty()) {
            positionInput.error = "Введите должность / специальность"
            positionInput.requestFocus()
            return false
        }
        if (department.isEmpty()) {
            departmentInput.error = "Введите отделение"
            departmentInput.requestFocus()
            return false
        }
        if (licenseNumber.isEmpty()) {
            licenseNumberInput.error = "Введите номер лицензии"
            licenseNumberInput.requestFocus()
            return false
        }
        return true
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}