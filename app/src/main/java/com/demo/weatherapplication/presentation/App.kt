package com.demo.weatherapplication.presentation

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    lateinit var mContext : Context

    override fun onCreate() {
        super.onCreate()
        mContext = this
    }
}