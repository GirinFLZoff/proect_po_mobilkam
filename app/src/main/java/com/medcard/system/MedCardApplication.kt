package com.medcard.system

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class MedCardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val appLocale = LocaleListCompat.forLanguageTags("ru")
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}