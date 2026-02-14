package com.medcard.system.model

import java.io.Serializable
import java.util.Date

/**
 * Модель данных пациента согласно требованиям 152-ФЗ и HIPAA
 */
data class Patient(
    val id: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val dateOfBirth: Date? = null,
    val gender: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val insuranceNumber: String = "",
    val passportSeries: String = "",
    val passportNumber: String = "",
    val bloodType: String = "",
    val allergies: List<String> = emptyList(),
    val chronicDiseases: List<String> = emptyList(),
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val medicalHistory: List<MedicalRecord> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val createdBy: String = "",
    val lastAccessedBy: String = "",
    val encryptionStatus: Boolean = true
) : Serializable {
    
    fun getFullName(): String {
        return "$lastName $firstName $middleName".trim()
    }
    
    fun getAge(): Int {
        if (dateOfBirth == null) return 0
        val diff = Date().time - dateOfBirth.time
        return (diff / (1000L * 60 * 60 * 24 * 365)).toInt()
    }
}

data class MedicalRecord(
    val id: String = "",
    val patientId: String = "",
    val date: Date = Date(),
    val doctorName: String = "",
    val diagnosis: String = "",
    val complaints: String = "",
    val examination: String = "",
    val treatment: String = "",
    val prescriptions: List<Prescription> = emptyList(),
    val labResults: List<LabResult> = emptyList(),
    val attachments: List<String> = emptyList(),
    val icd10Code: String = "",
    val createdAt: Date = Date()
) : Serializable

data class Prescription(
    val id: String = "",
    val medicationName: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = "",
    val prescribedBy: String = "",
    val prescribedDate: Date = Date()
) : Serializable

data class LabResult(
    val id: String = "",
    val testName: String = "",
    val result: String = "",
    val referenceRange: String = "",
    val unit: String = "",
    val date: Date = Date(),
    val laboratoryName: String = "",
    val status: String = "completed"
) : Serializable
