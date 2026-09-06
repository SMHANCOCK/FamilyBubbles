package com.familybubbles.widget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familybubbles.widget.data.FamilyPerson
import com.familybubbles.widget.databinding.ItemPersonBinding

class PersonAdapter(
    private val onEdit: (FamilyPerson) -> Unit,
    private val onDelete: (FamilyPerson) -> Unit
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    private val people = mutableListOf<FamilyPerson>()

    fun submitList(newPeople: List<FamilyPerson>) {
        people.clear()
        people.addAll(newPeople)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) = holder.bind(people[position])

    override fun getItemCount(): Int = people.size

    inner class PersonViewHolder(private val binding: ItemPersonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(person: FamilyPerson) {
            binding.nameText.text = person.name
            binding.phoneText.text = person.phone
            val bitmap = ImageUtils.loadBitmap(person.photoPath)
            if (bitmap != null) {
                binding.photoView.setImageBitmap(ImageUtils.circleCrop(bitmap, 180))
            } else {
                binding.photoView.setImageBitmap(ImageUtils.placeholder(180, person.name.take(2)))
            }
            binding.editButton.setOnClickListener { onEdit(person) }
            binding.deleteButton.setOnClickListener { onDelete(person) }
        }
    }
}
