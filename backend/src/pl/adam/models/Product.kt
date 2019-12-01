package pl.adam.models

import java.util.*

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val manufacturer: String,
    val model: String,
    val price: Int,
    val quantity: Int = 0
)
