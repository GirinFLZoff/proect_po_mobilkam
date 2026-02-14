package com.medcard.system.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.medcard.system.model.User
import com.medcard.system.model.UserRole
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Репозиторий для аутентификации пользователей
 */
class AuthRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    
    /**
     * Вход пользователя
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            
            if (user != null) {
                // Обновление времени последнего входа
                updateLastLogin(user.uid)
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Регистрация нового пользователя
     */
    suspend fun register(email: String, password: String, userData: User): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            
            if (user != null) {
                // Сохранение дополнительных данных пользователя
                val userWithId = userData.copy(
                    id = user.uid,
                    email = email,
                    createdAt = Date(),
                    isActive = true
                )
                usersCollection.document(user.uid).set(userWithId).await()
                
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Выход пользователя
     */
    fun logout() {
        auth.signOut()
    }
    
    /**
     * Получение текущего пользователя
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    /**
     * Получение данных пользователя из Firestore
     */
    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User data not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Обновление времени последнего входа
     */
    private suspend fun updateLastLogin(userId: String) {
        try {
            usersCollection.document(userId)
                .update("lastLogin", Date())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Проверка авторизации
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Сброс пароля
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
