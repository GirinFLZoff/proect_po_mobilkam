package com.medcard.system.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Класс для шифрования медицинских данных согласно 152-ФЗ
 * Использует AES-256-GCM для шифрования данных
 */
class EncryptionManager(private val context: Context) {
    
    companion object {
        private const val KEY_ALIAS = "MedCardEncryptionKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    init {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }
    
    /**
     * Генерация ключа шифрования в Android Keystore
     */
    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    /**
     * Получение ключа из хранилища
     */
    private fun getKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
    
    /**
     * Шифрование данных
     */
    fun encrypt(data: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        
        // Объединяем IV и зашифрованные данные
        return iv + encryptedData
    }
    
    /**
     * Расшифровка данных
     */
    fun decrypt(encryptedData: ByteArray): String {
        val iv = encryptedData.sliceArray(0 until IV_SIZE)
        val ciphertext = encryptedData.sliceArray(IV_SIZE until encryptedData.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        
        val decryptedData = cipher.doFinal(ciphertext)
        return String(decryptedData, StandardCharsets.UTF_8)
    }
    
    /**
     * Шифрование строки в Base64
     */
    fun encryptToString(data: String): String {
        val encrypted = encrypt(data)
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)
    }
    
    /**
     * Расшифровка строки из Base64
     */
    fun decryptFromString(encryptedString: String): String {
        val encrypted = android.util.Base64.decode(encryptedString, android.util.Base64.DEFAULT)
        return decrypt(encrypted)
    }
    
    /**
     * Получение безопасного SharedPreferences
     */
    fun getEncryptedSharedPreferences(): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        return EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
