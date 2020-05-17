package com.unlam.soa.utils

import android.app.Application
import com.unlam.soa.sharedPreferences.AppPreferences

class FitSoaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
    }
}