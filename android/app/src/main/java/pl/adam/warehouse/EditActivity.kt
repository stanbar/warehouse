package pl.adam.warehouse

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import pl.adam.warehouse.models.Product
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import kotlin.coroutines.CoroutineContext

class EditActivity : AppCompatActivity(), CoroutineScope {
    val TAG = "EditActivity "

    private val parent = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = parent + Dispatchers.Main

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this
        )
    }

    private fun getAccessToken() = prefs.getString("accessToken", null)

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
        setContentView(R.layout.activity_edit)
        val product = intent.getParcelableExtra<Product?>(PRODUCT)
        if (product != null) {
            edManufacturer.setText(product.manufacturer)
            edModel.setText(product.model)
            edPrice.setText(product.price.toString())
            edDelta.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
            btnApplyDelta.visibility = View.VISIBLE
            btnPut.text = "Update"
        } else {
            edDelta.visibility = View.GONE
            btnDelete.visibility = View.GONE
            btnApplyDelta.visibility = View.GONE
            btnPut.text = "Create"
        }

        btnDelete.setOnClickListener {
            if (product != null) {
                GlobalScope.launch {
                    service.deleteProducts(product.id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditActivity,
                            "Successfully deleted products",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }
            } else
                Toast.makeText(this, "Can not delete null product", Toast.LENGTH_SHORT).show()

        }
        btnApplyDelta.setOnClickListener {
            val delta = edDelta.text.toString().toIntOrNull()
            if (product != null && delta != null) {
                GlobalScope.launch {
                    service.changeQuantity(product.id, delta)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditActivity,
                            "Successfully changed quantity",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }
            } else
                Toast.makeText(this, "Can not convert delta text to int", Toast.LENGTH_SHORT).show()
        }

        btnPut.setOnClickListener {
            val newProduct = Product(
                UUID.randomUUID().toString(),
                edManufacturer.text.toString(),
                edModel.text.toString(),
                edPrice.text.toString().toInt(),
                0
            )
            if (product != null) {
                GlobalScope.launch {
                    service.updateProduct(product.id, newProduct)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditActivity,
                            "Successfully updated products",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }
            } else {
                GlobalScope.launch {
                    service.postProduct(newProduct)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditActivity,
                            "Successfully created products",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }
            }
        }
    }

    companion object {
        const val PRODUCT = "product"

    }
}
