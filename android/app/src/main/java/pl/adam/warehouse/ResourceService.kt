package pl.adam.warehouse

import pl.adam.warehouse.models.Product
import pl.adam.warehouse.models.User
import retrofit2.Response
import retrofit2.http.*

interface ResourceService {
    @GET("/currentUser")
    suspend fun getCurrentUser(): User

    @GET("/products")
    suspend fun getProducts(): List<Product>

    @GET("/products/{id}")
    suspend fun getProducts(@Path("id") productId: String): Product

    @POST("/products")
    suspend fun postProduct(@Body product: Product): Response<Void>

    @DELETE("/products/{id}")
    suspend fun deleteProducts(@Path("id") productId: String): Response<Void>

    @PUT("/products/{id}")
    suspend fun updateProduct(@Path("id") productId: String, @Body product: Product): Response<Void>

    @POST("/changeQuantity/{id}")
    suspend fun changeQuantity(@Path("id") productId: String, @Query("delta") delta: Int): Response<Void>
}

