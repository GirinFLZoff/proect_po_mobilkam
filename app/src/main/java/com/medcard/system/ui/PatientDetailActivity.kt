package com.medcard.system.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.medcard.system.R
import com.medcard.system.model.MedicalRecord
import com.medcard.system.ui.adapter.LabResultAdapter
import com.medcard.system.ui.adapter.MedicalRecordAdapter
import com.medcard.system.viewmodel.PatientViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Детальная информация о пациенте с табами
 */
class PatientDetailActivity : AppCompatActivity() {

    private val viewModel: PatientViewModel by viewModels()

    private lateinit var nameTextView: TextView
    private lateinit var ageTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var bloodTypeTextView: TextView
    private lateinit var insuranceTextView: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar

    // Allergies card
    private lateinit var cardAllergies: MaterialCardView
    private lateinit var allergiesTextView: TextView
    private lateinit var chronicTextView: TextView

    // Tab content containers
    private lateinit var tabMedicalHistory: LinearLayout
    private lateinit var tabLabResults: LinearLayout
    private lateinit var tabDocuments: LinearLayout

    // Medical history tab
    private lateinit var btnAddRecord: MaterialButton
    private lateinit var tvNoRecords: TextView
    private lateinit var rvMedicalHistory: RecyclerView
    private lateinit var medicalRecordAdapter: MedicalRecordAdapter

    // Lab results tab
    private lateinit var tvNoLabResults: TextView
    private lateinit var rvLabResults: RecyclerView
    private lateinit var labResultAdapter: LabResultAdapter

    // Documents tab
    private lateinit var tvPassport: TextView
    private lateinit var tvEmergencyContact: TextView
    private lateinit var tvEmergencyPhone: TextView

    private lateinit var fabEditPatient: FloatingActionButton

    private var patientId: String? = null

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            patientId?.let { viewModel.loadPatient(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_detail)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Карта пациента"

        patientId = intent.getStringExtra("patient_id")

        initViews()
        setupTabs()
        setupAdapters()
        setupListeners()
        setupObservers()

        if (patientId != null) {
            viewModel.loadPatient(patientId!!)
        } else {
            Toast.makeText(this, "Ошибка: ID пациента не найден", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        nameTextView = findViewById(R.id.patientDetailName)
        ageTextView = findViewById(R.id.patientDetailAge)
        genderTextView = findViewById(R.id.patientDetailGender)
        phoneTextView = findViewById(R.id.patientDetailPhone)
        emailTextView = findViewById(R.id.patientDetailEmail)
        addressTextView = findViewById(R.id.patientDetailAddress)
        bloodTypeTextView = findViewById(R.id.patientDetailBloodType)
        insuranceTextView = findViewById(R.id.patientDetailInsurance)
        tabLayout = findViewById(R.id.tabLayout)
        progressBar = findViewById(R.id.progressBar)

        cardAllergies = findViewById(R.id.cardAllergies)
        allergiesTextView = findViewById(R.id.patientDetailAllergies)
        chronicTextView = findViewById(R.id.patientDetailChronic)

        tabMedicalHistory = findViewById(R.id.tabMedicalHistory)
        tabLabResults = findViewById(R.id.tabLabResults)
        tabDocuments = findViewById(R.id.tabDocuments)

        btnAddRecord = findViewById(R.id.btnAddRecord)
        tvNoRecords = findViewById(R.id.tvNoRecords)
        rvMedicalHistory = findViewById(R.id.rvMedicalHistory)

        tvNoLabResults = findViewById(R.id.tvNoLabResults)
        rvLabResults = findViewById(R.id.rvLabResults)

        tvPassport = findViewById(R.id.tvPassport)
        tvEmergencyContact = findViewById(R.id.tvEmergencyContact)
        tvEmergencyPhone = findViewById(R.id.tvEmergencyPhone)
        fabEditPatient = findViewById(R.id.fabEditPatient)
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("История болезни"))
        tabLayout.addTab(tabLayout.newTab().setText("Анализы"))
        tabLayout.addTab(tabLayout.newTab().setText("Документы"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showTab(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showTab(position: Int) {
        tabMedicalHistory.visibility = if (position == 0) View.VISIBLE else View.GONE
        tabLabResults.visibility = if (position == 1) View.VISIBLE else View.GONE
        tabDocuments.visibility = if (position == 2) View.VISIBLE else View.GONE
    }

    private fun setupAdapters() {
        medicalRecordAdapter = MedicalRecordAdapter()
        rvMedicalHistory.layoutManager = LinearLayoutManager(this)
        rvMedicalHistory.adapter = medicalRecordAdapter

        labResultAdapter = LabResultAdapter()
        rvLabResults.layoutManager = LinearLayoutManager(this)
        rvLabResults.adapter = labResultAdapter
    }

    private fun setupListeners() {
        btnAddRecord.setOnClickListener {
            showAddRecordDialog()
        }

        fabEditPatient.setOnClickListener {
            val id = patientId ?: return@setOnClickListener
            val intent = Intent(this, AddEditPatientActivity::class.java)
            intent.putExtra(AddEditPatientActivity.EXTRA_PATIENT_ID, id)
            editLauncher.launch(intent)
        }

        phoneTextView.setOnClickListener {
            val phone = viewModel.selectedPatient.value?.phone
            if (!phone.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                startActivity(intent)
            }
        }
    }

    private fun setupObservers() {
        viewModel.selectedPatient.observe(this) { patient ->
            patient?.let {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                nameTextView.text = it.getFullName()
                ageTextView.text = "Возраст: ${it.getAge()} лет"
                genderTextView.text = "Пол: ${it.gender.ifBlank { "—" }}"
                phoneTextView.text = "Телефон: ${it.phone.ifBlank { "—" }}"
                phoneTextView.isClickable = it.phone.isNotBlank()
                phoneTextView.paintFlags = if (it.phone.isNotBlank()) {
                    phoneTextView.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
                } else {
                    phoneTextView.paintFlags and android.graphics.Paint.UNDERLINE_TEXT_FLAG.inv()
                }
                emailTextView.text = "Email: ${it.email.ifBlank { "—" }}"
                addressTextView.text = "Адрес: ${it.address.ifBlank { "—" }}"
                bloodTypeTextView.text = "Группа крови: ${it.bloodType.ifBlank { "—" }}"
                insuranceTextView.text = "Полис ОМС: ${it.insuranceNumber.ifBlank { "—" }}"

                if (it.dateOfBirth != null) {
                    supportActionBar?.subtitle = "Дата рождения: ${dateFormat.format(it.dateOfBirth)}"
                }

                // Allergies card
                val hasAllergies = it.allergies.isNotEmpty()
                val hasChronic = it.chronicDiseases.isNotEmpty()
                if (hasAllergies || hasChronic) {
                    cardAllergies.visibility = View.VISIBLE
                    allergiesTextView.text = "Аллергии: ${if (hasAllergies) it.allergies.joinToString(", ") else "нет"}"
                    chronicTextView.text = "Хронические заболевания: ${if (hasChronic) it.chronicDiseases.joinToString(", ") else "нет"}"
                } else {
                    cardAllergies.visibility = View.GONE
                }

                // Medical history
                val records = it.medicalHistory.sortedByDescending { r -> r.date }
                medicalRecordAdapter.submitList(records)
                tvNoRecords.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE

                // Lab results from all medical records
                val allLabResults = it.medicalHistory
                    .flatMap { record -> record.labResults }
                    .sortedByDescending { lr -> lr.date }
                labResultAdapter.submitList(allLabResults)
                tvNoLabResults.visibility = if (allLabResults.isEmpty()) View.VISIBLE else View.GONE

                // Documents tab
                val passport = if (it.passportSeries.isNotBlank() && it.passportNumber.isNotBlank()) {
                    "${it.passportSeries} ${it.passportNumber}"
                } else {
                    "—"
                }
                tvPassport.text = "Паспорт: $passport"
                tvEmergencyContact.text = "Контакт: ${it.emergencyContact.ifBlank { "—" }}"
                tvEmergencyPhone.text = "Телефон: ${it.emergencyPhone.ifBlank { "—" }}"
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun showAddRecordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_medical_record, null)

        val etDiagnosis = dialogView.findViewById<TextInputEditText>(R.id.etDiagnosis)
        val etIcd10Code = dialogView.findViewById<TextInputEditText>(R.id.etIcd10Code)
        val etComplaints = dialogView.findViewById<TextInputEditText>(R.id.etComplaints)
        val etExamination = dialogView.findViewById<TextInputEditText>(R.id.etExamination)
        val etTreatment = dialogView.findViewById<TextInputEditText>(R.id.etTreatment)
        val etDoctorName = dialogView.findViewById<TextInputEditText>(R.id.etDoctorName)

        AlertDialog.Builder(this)
            .setTitle("Новая запись")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val diagnosis = etDiagnosis.text.toString().trim()
                if (diagnosis.isBlank()) {
                    Toast.makeText(this, "Введите диагноз", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val record = MedicalRecord(
                    date = Date(),
                    doctorName = etDoctorName.text.toString().trim(),
                    diagnosis = diagnosis,
                    complaints = etComplaints.text.toString().trim(),
                    examination = etExamination.text.toString().trim(),
                    treatment = etTreatment.text.toString().trim(),
                    icd10Code = etIcd10Code.text.toString().trim()
                )

                patientId?.let { id ->
                    viewModel.addMedicalRecord(id, record) {
                        Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
