package pl.adam.warehouse

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.coroutines.CoroutineContext


class HomeActivity : AppCompatActivity(), CoroutineScope {
    val TAG = "HomeActivity"
    private val parent = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = parent + Dispatchers.Main

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this
        )
    }

    private fun getAccessToken() = prefs.getString("accessToken", null)
    private fun clearAccessToken() = prefs.edit().putString("accessToken", null).apply()

    private val client by lazy {
        OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${getAccessToken()}")
                .build()
            chain.proceed(newRequest)
        }.build()
    }

    private val service: ResourceService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ResourceService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)
        rvProducts.adapter =
            ProductsAdapter { product ->
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra(EditActivity.PRODUCT, product)
                startActivity(intent)
            }
        fetchCurrentUser()
        fetchProducts()

        btnLogout.setOnClickListener {
            clearAccessToken()
            finish()
        }

        btnFetchProducts.setOnClickListener {
            fetchProducts()
        }
    }


    private fun fetchCurrentUser() = launch {
        val currentUser = try {
            withContext(Dispatchers.IO) {
                service.getCurrentUser()
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            return@launch
        }
        tvEmail.text = currentUser.email
        tvRole.text = currentUser.role
    }

    private fun fetchProducts() = launch {
        val products = withContext(Dispatchers.IO) { service.getProducts() }
        println(products)
        (rvProducts.adapter as ProductsAdapter).replaceProducts(products)
    }

    override fun onDestroy() {
        parent.cancel()
        super.onDestroy()
    }

}
