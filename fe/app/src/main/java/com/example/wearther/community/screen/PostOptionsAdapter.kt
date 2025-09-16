package com.example.wearther.community.screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wearther.databinding.ItemPostOptionBinding
import com.example.wearther.community.data.PostSourceOption

class PostOptionsAdapter(
    private val options: List<PostSourceOption>,
    private val onOptionClick: (PostSourceOption) -> Unit
) : RecyclerView.Adapter<PostOptionsAdapter.OptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemPostOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount(): Int = options.size

    inner class OptionViewHolder(private val binding: ItemPostOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(option: PostSourceOption) {
            with(binding) {
                imageIcon.setImageResource(option.iconRes)
                textTitle.text = option.title
                textDescription.text = option.description

                root.setOnClickListener {
                    onOptionClick(option)
                }
            }
        }
    }
}