package pl.adam.warehouse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.adam.warehouse.models.Product

class ProductsAdapter : RecyclerView.Adapter<ProductsAdapter.ProductVH>() {
    private val products = mutableListOf<Product>()

    fun replaceProducts(products: List<Product>){
        this.products.clear()
        this.products.addAll(products)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.product_view_holder, null)
        return ProductVH(view)
    }

    override fun getItemCount() = products.size
    override fun onBindViewHolder(holder: ProductVH, position: Int) {
        holder.bind(products[position])
    }

    class ProductVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvModel: TextView = view.findViewById(R.id.tvModel)
        private val tvManufacturer: TextView = view.findViewById(R.id.tvManufacturer)
        private val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        private val tvQuantities: TextView = view.findViewById(R.id.tvQuantities)

        fun bind(product: Product) {
            println("bind $product")
            tvModel.text = product.model
            tvManufacturer.text = product.manufacturer
            tvPrice.text = product.price.toString()
            tvQuantities.text = product.quantity.toString()
        }
    }

}