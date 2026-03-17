package com.android.dang.dictionary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.dang.R
import com.android.dang.databinding.ItemDictionaryBinding
import com.android.dang.dictionary.data.BreedsData
import com.bumptech.glide.Glide

class DictionaryListAdapter(
    private val onItemClick: (BreedsData.BreedsDataItem) -> Unit
) : RecyclerView.Adapter<DictionaryListAdapter.DictionaryItemViewHolder>() {

    private val items = ArrayList<BreedsData.BreedsDataItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryItemViewHolder {
        return DictionaryItemViewHolder(
            ItemDictionaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DictionaryItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun addItems(resData: List<BreedsData.BreedsDataItem>, isClear: Boolean) {
        if (isClear) {
            items.clear()
        }
        items.addAll(resData)
        notifyDataSetChanged()
    }

    inner class DictionaryItemViewHolder(private val binding: ItemDictionaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BreedsData.BreedsDataItem) {
            val context = binding.root.context
            binding.dogName.text = BreedNameLocalizer.localize(context, item.name)
            binding.dogInfoDictionary.text = DictionaryBreedUi.cardSummary(context, item)

            Glide.with(binding.dogImg.context)
                .load(DictionaryBreedUi.imageUrl(item))
                .placeholder(R.drawable.icon_dog1)
                .error(R.drawable.icon_dog1)
                .into(binding.dogImg)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
