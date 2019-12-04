package pl.adam.warehouse.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val id: String,
    val manufacturer: String,
    val model: String,
    val price: Int,
    val quantity: Int
) : Parcelable