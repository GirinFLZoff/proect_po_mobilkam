package com.medcard.system.model

import java.io.Serializable
import java.util.Date

/**
 * Модель журнала аудита для соответствия требованиям безопасности
 */
data class AuditLog(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val action: AuditAction = AuditAction.VIEW,
    val resourceType: String = "",
    val resourceId: String = "",
    val timestamp: Date = Date(),
    val ipAddress: String = "",
    val deviceInfo: String = "",
    val success: Boolean = true,
    val details: String = ""
) : Serializable

enum class AuditAction {
    VIEW,
    CREATE,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    EXPORT,
    PRINT,
    SHARE,
    SEARCH
}
