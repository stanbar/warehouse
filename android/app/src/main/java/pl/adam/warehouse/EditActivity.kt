package pl.adam.warehouse

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.coroutines.*
import pl.adam.warehouse.models.Product
import retrofit2.Response
import java.util.*

class EditActivity : AppCompatActivity() {

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this
        )
    }
    private val resService by lazy {
        ResourceService(LocalStorage(prefs))
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

        btnDelete.setOnClickListener { onDeleteClick(product) }
        btnApplyDelta.setOnClickListener { onApplyChangeClick(product) }
        btnPut.setOnClickListener { onPutClick(product) }
    }

    private fun onPutClick(product: Product?) {
        val newProduct = Product(
            UUID.randomUUID().toString(),
            edManufacturer.text.toString(),
            edModel.text.toString(),
            edPrice.text.toString().toInt(),
            0
        )
        if (product != null) {
            GlobalScope.launch {
                val response = resService.updateProduct(product.id, newProduct)
                showResponseToUi(
                    response,
                    "Successfully updated  product",
                    "Failed to update product"
                )
            }
        } else {
            GlobalScope.launch {
                val response = resService.postProduct(newProduct)
                showResponseToUi(
                    response,
                    "Successfully created product",
                    "Failed to create product"
                )
            }
        }
    }

    private fun onDeleteClick(product: Product?) {
        if (product != null) {
            GlobalScope.launch {
                val response = resService.deleteProducts(product.id)
                showResponseToUi(
                    response,
                    "Successfully deleted product",
                    "Failed to delete product"
                )
            }
        } else
            Toast.makeText(this, "Can not delete null product", Toast.LENGTH_SHORT).show()
    }

    private fun onApplyChangeClick(product: Product?) {
        val delta = edDelta.text.toString().toIntOrNull()
        if (product != null && delta != null) {
            GlobalScope.launch {
                val response = resService.changeQuantity(product.id, delta)
                showResponseToUi(
                    response,
                    "Successfully changed quantity",
                    "Failed to change quantity"
                )
            }
        } else
            Toast.makeText(this, "Can not convert delta text to int", Toast.LENGTH_SHORT).show()
    }

    private suspend fun showResponseToUi(
        response: Response<Void>,
        successMessage: String,
        failMessage: String
    ) {
        if (response.isSuccessful) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@EditActivity,
                    successMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
            finish()
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@EditActivity,
                    failMessage + ": " + response.code(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val PRODUCT = "product"

    }
}
