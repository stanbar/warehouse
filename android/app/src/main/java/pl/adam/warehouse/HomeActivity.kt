package pl.adam.warehouse

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class HomeActivity : AppCompatActivity(), CoroutineScope {
    private val TAG = "HomeActivity"
    private val parent = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = parent + Dispatchers.Main

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this
        )
    }

    private val authService by lazy { LocalStorage(prefs) }
    private val resService by lazy { ResourceService(authService) }

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

        setListeners()
    }

    private fun fetchCurrentUser() = launch {
        val currentUser = try {
            withContext(Dispatchers.IO) {
                resService.getCurrentUser()
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            return@launch
        }
        tvEmail.text = currentUser.email
        tvRole.text = currentUser.role
    }

    private fun fetchProducts() = launch {
        val products = withContext(Dispatchers.IO) { resService.getProducts() }
        println(products)
        (rvProducts.adapter as ProductsAdapter).replaceProducts(products)
    }

    private fun setListeners() {
        btnLogout.setOnClickListener {
            authService.clearAccessToken()
            finish()
        }
        btnPullProducts.setOnClickListener {
            fetchProducts()
        }
        btnAddProduct.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        parent.cancel() // Cancel out, parent coroutine scope, so they don't won't execute any following callbacks
        super.onDestroy()
    }

}
