package com.medcard.system.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.medcard.system.R
import com.medcard.system.model.Patient
import com.medcard.system.ui.adapter.PatientAdapter
import com.medcard.system.viewmodel.AuthViewModel
import com.medcard.system.viewmodel.PatientViewModel

/**
 * Главная активность со списком пациентов
 */
class MainActivity : AppCompatActivity() {

    private val patientViewModel: PatientViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PatientAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private val addEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            patientViewModel.loadPatients()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверка авторизации
        if (!authViewModel.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Медицинские карты пациентов"

        initViews()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (authViewModel.isUserLoggedIn()) {
            patientViewModel.loadPatients()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.patientsRecyclerView)
        fabAdd = findViewById(R.id.fabAddPatient)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupRecyclerView() {
        adapter = PatientAdapter(
            onItemClick = { patient ->
                openPatientDetail(patient)
            },
            onEditClick = { patient ->
                openEditPatient(patient)
            },
            onDeleteClick = { patient ->
                deletePatient(patient)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        patientViewModel.patients.observe(this) { patients ->
            adapter.submitList(patients)
            tvEmpty.visibility = if (patients.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        patientViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                patientViewModel.clearError()
            }
        }

        patientViewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        authViewModel.userData.observe(this) { user ->
            user?.let {
                supportActionBar?.subtitle = it.getFullName()
            }
        }
    }

    private fun setupListeners() {
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditPatientActivity::class.java)
            addEditLauncher.launch(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { patientViewModel.searchPatients(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { patientViewModel.searchPatients(it) }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_audit_log -> {
                startActivity(Intent(this, AuditLogActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                authViewModel.logout()
                navigateToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openPatientDetail(patient: Patient) {
        val intent = Intent(this, PatientDetailActivity::class.java)
        intent.putExtra("patient_id", patient.id)
        startActivity(intent)
    }

    private fun openEditPatient(patient: Patient) {
        val intent = Intent(this, AddEditPatientActivity::class.java)
        intent.putExtra(AddEditPatientActivity.EXTRA_PATIENT_ID, patient.id)
        addEditLauncher.launch(intent)
    }

    private fun deletePatient(patient: Patient) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Удаление пациента")
            .setMessage("Вы уверены, что хотите удалить карту пациента ${patient.getFullName()}?")
            .setPositiveButton("Удалить") { _, _ ->
                patientViewModel.deletePatient(patient.id) {
                    Toast.makeText(this, "Пациент удален", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
