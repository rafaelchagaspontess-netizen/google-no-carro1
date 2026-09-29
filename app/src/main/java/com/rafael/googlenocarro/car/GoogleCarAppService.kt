package com.rafael.googlenocarro.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/** Ponto de entrada do Android Auto. */
class GoogleCarAppService : CarAppService() {
    // App pessoal instalado por APK: aceita qualquer host (Android Auto / Automotive).
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen = HomeScreen(carContext)
    }
}
