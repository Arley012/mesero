package com.restaurante.mesero

import android.app.Application
import com.restaurante.mesero.data.AppContainer

class MeseroApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer.getInstance(this)
    }
}
