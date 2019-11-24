package pl.adam.models

data class Product(
    val id: String,
    val manufacturer: String,
    val model: String,
    val price: Int,
    val quantity: Int
)
