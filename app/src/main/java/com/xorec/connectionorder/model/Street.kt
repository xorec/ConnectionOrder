package com.xorec.connectionorder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Street(
    @SerializedName("street_id") val streetId: Long,
    @SerializedName("street") val street: String
): Serializable {
    override fun toString(): String {
        return street
    }
}