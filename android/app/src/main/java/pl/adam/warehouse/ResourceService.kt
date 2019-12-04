package pl.adam.warehouse

import okhttp3.OkHttpClient
import okhttp3.Request
import pl.adam.warehouse.models.Product
import pl.adam.warehouse.models.User
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface IResourceService {
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

class ResourceService(private val localStorage: LocalStorage) :
    IResourceService {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${localStorage.getAccessToken()}")
                .build()
            chain.proceed(newRequest)
        }.build()
    }

    private val service: IResourceService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(IResourceService::class.java)
    }

    override suspend fun getCurrentUser() = service.getCurrentUser()

    override suspend fun getProducts() = service.getProducts()

    override suspend fun getProducts(productId: String) = service.getProducts(productId)

    override suspend fun postProduct(product: Product) = service.postProduct(product)

    override suspend fun deleteProducts(productId: String) = service.deleteProducts(productId)

    override suspend fun updateProduct(productId: String, product: Product) =
        service.updateProduct(productId, product)

    override suspend fun changeQuantity(productId: String, delta: Int) =
        service.changeQuantity(productId, delta)

}
