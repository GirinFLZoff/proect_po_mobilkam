package com.medcard.system.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.medcard.system.R
import com.medcard.system.model.Patient
import com.medcard.system.viewmodel.PatientViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Активность для добавления и редактирования пациента
 */
class AddEditPatientActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()

    private lateinit var etLastName: TextInputEditText
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etMiddleName: TextInputEditText
    private lateinit var etDateOfBirth: TextInputEditText
    private lateinit var actvGender: AutoCompleteTextView
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etInsuranceNumber: TextInputEditText
    private lateinit var etPassportSeries: TextInputEditText
    private lateinit var etPassportNumber: TextInputEditText
    private lateinit var actvBloodType: AutoCompleteTextView
    private lateinit var etAllergies: TextInputEditText
    private lateinit var etChronicDiseases: TextInputEditText
    private lateinit var etEmergencyContact: TextInputEditText
    private lateinit var etEmergencyPhone: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var selectedDateOfBirth: Date? = null
    private var editingPatientId: String? = null
    private var isEditMode = false

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_patient)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editingPatientId = intent.getStringExtra(EXTRA_PATIENT_ID)
        isEditMode = editingPatientId != null

        supportActionBar?.title = if (isEditMode) "Редактирование пациента" else "Новый пациент"

        initViews()
        setupDropdowns()
        setupListeners()
        setupObservers()

        if (isEditMode) {
            viewModel.loadPatient(editingPatientId!!)
        }
    }

    private fun initViews() {
        etLastName = findViewById(R.id.etLastName)
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        actvGender = findViewById(R.id.actvGender)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etInsuranceNumber = findViewById(R.id.etInsuranceNumber)
        etPassportSeries = findViewById(R.id.etPassportSeries)
        etPassportNumber = findViewById(R.id.etPassportNumber)
        actvBloodType = findViewById(R.id.actvBloodType)
        etAllergies = findViewById(R.id.etAllergies)
        etChronicDiseases = findViewById(R.id.etChronicDiseases)
        etEmergencyContact = findViewById(R.id.etEmergencyContact)
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupDropdowns() {
        val genders = arrayOf("Мужской", "Женский")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        actvGender.setAdapter(genderAdapter)

        val bloodTypes = arrayOf(
            "O(I) Rh+", "O(I) Rh-",
            "A(II) Rh+", "A(II) Rh-",
            "B(III) Rh+", "B(III) Rh-",
            "AB(IV) Rh+", "AB(IV) Rh-"
        )
        val bloodTypeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodTypes)
        actvBloodType.setAdapter(bloodTypeAdapter)
    }

    private fun setupListeners() {
        etDateOfBirth.setOnClickListener {
            showDatePicker()
        }

        btnSave.setOnClickListener {
            if (validateFields()) {
                savePatient()
            }
        }
    }

    private fun setupObservers() {
        viewModel.selectedPatient.observe(this) { patient ->
            patient?.let { fillForm(it) }
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            btnSave.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun fillForm(patient: Patient) {
        etLastName.setText(patient.lastName)
        etFirstName.setText(patient.firstName)
        etMiddleName.setText(patient.middleName)
        if (patient.dateOfBirth != null) {
            selectedDateOfBirth = patient.dateOfBirth
            etDateOfBirth.setText(dateFormat.format(patient.dateOfBirth))
        }
        actvGender.setText(patient.gender, false)
        etPhone.setText(patient.phone)
        etEmail.setText(patient.email)
        etAddress.setText(patient.address)
        etInsuranceNumber.setText(patient.insuranceNumber)
        etPassportSeries.setText(patient.passportSeries)
        etPassportNumber.setText(patient.passportNumber)
        actvBloodType.setText(patient.bloodType, false)
        etAllergies.setText(patient.allergies.joinToString(", "))
        etChronicDiseases.setText(patient.chronicDiseases.joinToString(", "))
        etEmergencyContact.setText(patient.emergencyContact)
        etEmergencyPhone.setText(patient.emergencyPhone)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        selectedDateOfBirth?.let { calendar.time = it }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDateOfBirth = calendar.time
                etDateOfBirth.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (etLastName.text.isNullOrBlank()) {
            etLastName.error = "Введите фамилию"
            isValid = false
        }
        if (etFirstName.text.isNullOrBlank()) {
            etFirstName.error = "Введите имя"
            isValid = false
        }
        if (selectedDateOfBirth == null) {
            etDateOfBirth.error = "Выберите дату рождения"
            isValid = false
        }
        if (actvGender.text.isNullOrBlank()) {
            actvGender.error = "Выберите пол"
            isValid = false
        }
        if (etPhone.text.isNullOrBlank()) {
            etPhone.error = "Введите телефон"
            isValid = false
        }

        return isValid
    }

    private fun savePatient() {
        val allergiesList = etAllergies.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val chronicDiseasesList = etChronicDiseases.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val existingPatient = viewModel.selectedPatient.value

        val patient = Patient(
            id = editingPatientId ?: "",
            lastName = etLastName.text.toString().trim(),
            firstName = etFirstName.text.toString().trim(),
            middleName = etMiddleName.text.toString().trim(),
            dateOfBirth = selectedDateOfBirth,
            gender = actvGender.text.toString(),
            address = etAddress.text.toString().trim(),
            phone = etPhone.text.toString().trim(),
            email = etEmail.text.toString().trim(),
            insuranceNumber = etInsuranceNumber.text.toString().trim(),
            passportSeries = etPassportSeries.text.toString().trim(),
            passportNumber = etPassportNumber.text.toString().trim(),
            bloodType = actvBloodType.text.toString(),
            allergies = allergiesList,
            chronicDiseases = chronicDiseasesList,
            emergencyContact = etEmergencyContact.text.toString().trim(),
            emergencyPhone = etEmergencyPhone.text.toString().trim(),
            medicalHistory = existingPatient?.medicalHistory ?: emptyList(),
            createdAt = existingPatient?.createdAt ?: Date(),
            createdBy = existingPatient?.createdBy ?: ""
        )

        if (isEditMode) {
            viewModel.updatePatient(patient) {
                Toast.makeText(this, "Данные пациента обновлены", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        } else {
            viewModel.addPatient(patient) { newPatientId ->
                Toast.makeText(this, "Пациент добавлен", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PatientDetailActivity::class.java)
                intent.putExtra("patient_id", newPatientId)
                startActivity(intent)
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
