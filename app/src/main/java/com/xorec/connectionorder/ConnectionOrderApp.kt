package com.xorec.connectionorder

import android.app.Application
import com.xorec.connectionorder.repository.AddressesRepository

/* Класс ConnectionOrderApp содержит singleton-объект AddressesRepository,
   который создается единожды в течение времени жизни процесса приложения. */
class ConnectionOrderApp: Application() {
    lateinit var addresses: AddressesRepository
        private set

    companion object {
        lateinit var instance: ConnectionOrderApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        addresses = AddressesRepository.getInstance()
    }
}