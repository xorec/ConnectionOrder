package com.xorec.connectionorder.api_calls

import com.xorec.connectionorder.model.House
import com.xorec.connectionorder.model.Street
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AddressesAPIService {
    @GET("utils/get/allStreets/")
    fun getStreets(): Call<List<Street>>

    @GET("utils/get/houses/")
    fun getHouses(@Query("street_id") streetId: Long): Call<List<House>>
}