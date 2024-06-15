package com.android.carepet.view.medicine

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.carepet.R
import com.android.carepet.data.response.Product
import com.android.carepet.databinding.ItemMedicineBinding
import com.bumptech.glide.Glide

class MedicineAdapter(private val products: List<Product>) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    class MedicineViewHolder(private val binding: ItemMedicineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.product = product
            binding.executePendingBindings()

            Glide.with(binding.imageView.context)
                .load(product.photo)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(binding.imageView)

            binding.btnBuy.setOnClickListener {
                val context = binding.btnBuy.context
                val shopUrl = product.linkStore
                if (!shopUrl.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(shopUrl)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Shop URL is not available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemMedicineBinding.inflate(layoutInflater, parent, false)
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int {
        return products.size
    }
}
