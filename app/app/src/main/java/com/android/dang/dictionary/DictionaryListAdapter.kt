package com.android.dang.dictionary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.dang.R
import com.android.dang.databinding.ItemDictionaryBinding
import com.android.dang.dictionary.data.BreedsData
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DictionaryListAdapter(
    private val scope: CoroutineScope,
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

    override fun onViewRecycled(holder: DictionaryItemViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    fun addItems(resData: List<BreedsData.BreedsDataItem>, isClear: Boolean) {
        if (isClear) {
            items.clear()
        }
        items.addAll(resData)
        notifyDataSetChanged()
    }

    inner class DictionaryItemViewHolder(private val binding: ItemDictionaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var imageJob: Job? = null
        private var boundBreedId: Int? = null

        fun bind(item: BreedsData.BreedsDataItem) {
            val context = binding.root.context
            boundBreedId = item.id
            imageJob?.cancel()
            binding.dogName.text = BreedNameLocalizer.localize(context, item.name)
            binding.dogInfoDictionary.text = DictionaryBreedUi.cardSummary(context, item)

            val initialImageUrl = DictionaryBreedUi.imageUrl(item)
            if (initialImageUrl.isNullOrBlank()) {
                binding.dogImg.setImageResource(R.drawable.icon_dog1)
            } else {
                Glide.with(binding.dogImg.context)
                    .load(initialImageUrl)
                    .placeholder(R.drawable.icon_dog1)
                    .error(R.drawable.icon_dog1)
                    .into(binding.dogImg)
            }

            imageJob = scope.launch {
                val imageUrl = BreedImageRepository.resolveImageUrl(item) ?: return@launch
                if (!isActive || boundBreedId != item.id) {
                    return@launch
                }

                Glide.with(binding.dogImg.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.icon_dog1)
                    .error(R.drawable.icon_dog1)
                    .into(binding.dogImg)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        fun recycle() {
            imageJob?.cancel()
            boundBreedId = null
        }
    }
}
