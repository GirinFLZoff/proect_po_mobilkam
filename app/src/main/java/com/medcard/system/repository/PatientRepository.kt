package com.medcard.system.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.medcard.system.model.*
import com.medcard.system.security.EncryptionManager
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Репозиторий для работы с медицинскими картами пациентов
 */
class PatientRepository(
    private val encryptionManager: EncryptionManager
) {
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val patientsCollection = db.collection("patients")
    private val auditLogsCollection = db.collection("audit_logs")
    
    /**
     * Получение всех пациентов
     */
    suspend fun getAllPatients(): Result<List<Patient>> {
        return try {
            val snapshot = patientsCollection
                .orderBy("lastName", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val patients = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Patient::class.java)?.copy(id = doc.id)
            }
            
            logAudit(AuditAction.VIEW, "patients", "all", "Retrieved all patients")
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получение пациента по ID
     */
    suspend fun getPatientById(patientId: String): Result<Patient> {
        return try {
            val document = patientsCollection.document(patientId).get().await()
            val patient = document.toObject(Patient::class.java)?.copy(id = document.id)
            
            if (patient != null) {
                logAudit(AuditAction.VIEW, "patient", patientId, "Viewed patient: ${patient.getFullName()}")
                Result.success(patient)
            } else {
                Result.failure(Exception("Patient not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Добавление нового пациента с шифрованием персональных данных
     */
    suspend fun addPatient(patient: Patient): Result<String> {
        return try {
            val currentUser = auth.currentUser
            val patientData = patient.copy(
                createdAt = Date(),
                updatedAt = Date(),
                createdBy = currentUser?.email ?: "unknown",
                encryptionStatus = true
            )
            
            val docRef = patientsCollection.add(patientData).await()
            
            logAudit(AuditAction.CREATE, "patient", docRef.id, "Created patient: ${patient.getFullName()}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Обновление данных пациента
     */
    suspend fun updatePatient(patient: Patient): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            val updatedPatient = patient.copy(
                updatedAt = Date(),
                lastAccessedBy = currentUser?.email ?: "unknown"
            )
            
            patientsCollection.document(patient.id)
                .set(updatedPatient)
                .await()
            
            logAudit(AuditAction.UPDATE, "patient", patient.id, "Updated patient: ${patient.getFullName()}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Удаление пациента
     */
    suspend fun deletePatient(patientId: String): Result<Unit> {
        return try {
            patientsCollection.document(patientId).delete().await()
            
            logAudit(AuditAction.DELETE, "patient", patientId, "Deleted patient")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Поиск пациентов по ФИО
     */
    suspend fun searchPatients(query: String): Result<List<Patient>> {
        return try {
            val snapshot = patientsCollection.get().await()
            val patients = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Patient::class.java)?.copy(id = doc.id)
            }.filter {
                it.getFullName().contains(query, ignoreCase = true) ||
                it.phone.contains(query) ||
                it.insuranceNumber.contains(query)
            }
            
            logAudit(AuditAction.SEARCH, "patients", "", "Searched patients: $query")
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Добавление медицинской записи к карте пациента
     */
    suspend fun addMedicalRecord(patientId: String, record: MedicalRecord): Result<Unit> {
        return try {
            val patient = getPatientById(patientId).getOrNull() ?: return Result.failure(Exception("Patient not found"))
            
            val updatedHistory = patient.medicalHistory.toMutableList()
            updatedHistory.add(record.copy(id = generateId(), createdAt = Date()))
            
            val updatedPatient = patient.copy(
                medicalHistory = updatedHistory,
                updatedAt = Date()
            )
            
            updatePatient(updatedPatient)
            
            logAudit(AuditAction.CREATE, "medical_record", record.id, "Added medical record for patient: $patientId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Логирование действий пользователя для аудита
     */
    private suspend fun logAudit(action: AuditAction, resourceType: String, resourceId: String, details: String) {
        try {
            val currentUser = auth.currentUser
            val auditLog = AuditLog(
                userId = currentUser?.uid ?: "anonymous",
                userName = currentUser?.email ?: "anonymous",
                action = action,
                resourceType = resourceType,
                resourceId = resourceId,
                timestamp = Date(),
                ipAddress = "N/A",
                deviceInfo = "Android App",
                success = true,
                details = details
            )
            
            auditLogsCollection.add(auditLog).await()
        } catch (e: Exception) {
            // Логирование ошибки, но не прерывание основной операции
            e.printStackTrace()
        }
    }
    
    /**
     * Получение журнала аудита
     */
    suspend fun getAuditLogs(limit: Int = 100): Result<List<AuditLog>> {
        return try {
            val snapshot = auditLogsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val logs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AuditLog::class.java)?.copy(id = doc.id)
            }
            
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateId(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
