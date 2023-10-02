package com.xorec.connectionorder.repository

import com.xorec.connectionorder.api_calls.AddressesAPIService
import com.xorec.connectionorder.model.House
import com.xorec.connectionorder.model.Street
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

/* Класс AddressesRepository нужен, чтобы создать сервис Retrofit и получать доступ к вызовам API.
*  Объект класса AddressesRepository создается единожды в течение времени жизни процесса приложения. */
class AddressesRepository private constructor() {
    private val service = Retrofit.Builder()
        .baseUrl("https://stat-api.airnet.ru/v2/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create<AddressesAPIService>()

    companion object {
        @Volatile
        private var instance: AddressesRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: AddressesRepository().also { instance = it }
            }
    }

    fun getStreets(): Call<List<Street>> {
        return service.getStreets()
    }

    fun getHouses(street: Street): Call<List<House>> {
        return service.getHouses(street.streetId)
    }
}