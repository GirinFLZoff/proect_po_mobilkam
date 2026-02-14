package com.medcard.system.model

import java.io.Serializable
import java.util.Date

/**
 * Модель пользователя системы с ролевым доступом
 */
data class User(
    val id: String = "",
    val email: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val role: UserRole = UserRole.DOCTOR,
    val position: String = "",
    val department: String = "",
    val licenseNumber: String = "",
    val phone: String = "",
    val createdAt: Date = Date(),
    val lastLogin: Date? = null,
    val isActive: Boolean = true,
    val permissions: List<Permission> = emptyList()
) : Serializable {
    
    fun getFullName(): String {
        return "$lastName $firstName $middleName".trim()
    }
}

enum class UserRole {
    ADMIN,
    DOCTOR,
    NURSE,
    REGISTRAR,
    LAB_TECHNICIAN
}

enum class Permission {
    READ_PATIENT,
    WRITE_PATIENT,
    DELETE_PATIENT,
    READ_MEDICAL_RECORD,
    WRITE_MEDICAL_RECORD,
    PRESCRIBE_MEDICATION,
    VIEW_LAB_RESULTS,
    MANAGE_USERS,
    AUDIT_LOG_ACCESS
}
