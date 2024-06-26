package com.android.carepet.view.disease

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.carepet.R
import com.android.carepet.databinding.ItemDiseaseBinding
import com.android.carepet.data.response.DiseaseResponse
import com.android.carepet.data.utils.StringUtils
import com.bumptech.glide.Glide

class DiseaseAdapter(private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder>() {
    private var diseases: List<DiseaseResponse> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val binding = ItemDiseaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiseaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        holder.bind(diseases[position])
    }

    override fun getItemCount(): Int = diseases.size

    fun submitList(list: List<DiseaseResponse>) {
        diseases = list
        notifyDataSetChanged()
    }

    inner class DiseaseViewHolder(private val binding: ItemDiseaseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(disease: DiseaseResponse) {
            binding.disease = disease
            binding.executePendingBindings()

            binding.diseaseDescription.text = StringUtils.truncateDescription(disease.description, 100)

            Glide.with(binding.imageView.context)
                .load(disease.photo)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(binding.imageView)

            itemView.setOnClickListener {
                onItemClick(disease.id)
            }
        }
    }
}
