package com.android.carepet.view.dogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.carepet.R
import com.android.carepet.data.response.DogResponse
import com.bumptech.glide.Glide

class DogsAdapter(
    private val deleteClickListener: (DogResponse) -> Unit,
    private val editClickListener: (DogResponse) -> Unit,
    private val detailClickListener: (DogResponse) -> Unit
) : RecyclerView.Adapter<DogsAdapter.DogViewHolder>() {

    private var dogs: List<DogResponse> = emptyList()

    fun submitList(dogs: List<DogResponse>?) {
        this.dogs = dogs ?: emptyList()
        notifyDataSetChanged()
    }

    fun getDogs(): List<DogResponse> = dogs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogs[position]
        holder.bind(dog)
        holder.itemView.findViewById<Button>(R.id.buttonDeleteDog).setOnClickListener {
            deleteClickListener(dog)
        }
        holder.itemView.findViewById<Button>(R.id.buttonEditDog).setOnClickListener {
            editClickListener(dog)
        }
        holder.itemView.findViewById<Button>(R.id.buttonDetail).setOnClickListener {
            detailClickListener(dog)
        }

    }

    override fun getItemCount(): Int = dogs.size

    class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDogName: TextView = itemView.findViewById(R.id.textViewDogName)
        private val textViewDogGender: TextView = itemView.findViewById(R.id.textViewDogGender)
        private val textViewDogBreed: TextView = itemView.findViewById(R.id.textViewDogBreed)
        private val textViewDogBirthday: TextView = itemView.findViewById(R.id.textViewDogBirthday)
        private val textViewDogAge: TextView = itemView.findViewById(R.id.textViewDogAge)
        private val imageViewDogPhoto: ImageView = itemView.findViewById(R.id.imageViewDogPhoto)

        fun bind(dog: DogResponse) {
            textViewDogName.text = dog.name
            textViewDogGender.text = dog.gender
            textViewDogBreed.text = dog.breed
            textViewDogBirthday.text = dog.birthday
            textViewDogAge.text = dog.age.toString()
            Glide.with(itemView.context)
                .load(dog.photo)
                .into(imageViewDogPhoto)
        }
    }
}
