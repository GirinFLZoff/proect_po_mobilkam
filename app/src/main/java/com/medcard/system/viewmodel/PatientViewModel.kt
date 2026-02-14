package com.medcard.system.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medcard.system.model.MedicalRecord
import com.medcard.system.model.Patient
import com.medcard.system.repository.PatientRepository
import com.medcard.system.security.EncryptionManager
import kotlinx.coroutines.launch

/**
 * ViewModel для управления списком пациентов
 */
class PatientViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: PatientRepository
    
    private val _patients = MutableLiveData<List<Patient>>()
    val patients: LiveData<List<Patient>> = _patients
    
    private val _selectedPatient = MutableLiveData<Patient?>()
    val selectedPatient: LiveData<Patient?> = _selectedPatient
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        val encryptionManager = EncryptionManager(application)
        repository = PatientRepository(encryptionManager)
        loadPatients()
    }
    
    /**
     * Загрузка списка пациентов
     */
    fun loadPatients() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.getAllPatients()
            
            result.onSuccess { patientList ->
                _patients.value = patientList
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _loading.value = false
        }
    }
    
    /**
     * Загрузка пациента по ID
     */
    fun loadPatient(patientId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.getPatientById(patientId)
            
            result.onSuccess { patient ->
                _selectedPatient.value = patient
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _loading.value = false
        }
    }
    
    /**
     * Добавление нового пациента
     */
    fun addPatient(patient: Patient, onSuccess: (patientId: String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.addPatient(patient)

            result.onSuccess { patientId ->
                loadPatients()
                onSuccess(patientId)
            }.onFailure { exception ->
                _error.value = exception.message
            }

            _loading.value = false
        }
    }
    
    /**
     * Обновление данных пациента
     */
    fun updatePatient(patient: Patient, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.updatePatient(patient)
            
            result.onSuccess {
                loadPatients()
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _loading.value = false
        }
    }
    
    /**
     * Удаление пациента
     */
    fun deletePatient(patientId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.deletePatient(patientId)
            
            result.onSuccess {
                loadPatients()
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _loading.value = false
        }
    }
    
    /**
     * Поиск пациентов
     */
    fun searchPatients(query: String) {
        if (query.isBlank()) {
            loadPatients()
            return
        }
        
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.searchPatients(query)
            
            result.onSuccess { patientList ->
                _patients.value = patientList
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _loading.value = false
        }
    }
    
    /**
     * Добавление медицинской записи
     */
    fun addMedicalRecord(patientId: String, record: MedicalRecord, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            val result = repository.addMedicalRecord(patientId, record)
            
            result.onSuccess {
                loadPatient(patientId)
                onSuccess()
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
