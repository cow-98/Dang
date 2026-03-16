package com.android.dang.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.dang.databinding.ItemCommonDetailBinding
import com.android.dang.databinding.ItemRecyclerViewRecentWordBinding
import com.android.dang.search.searchItemModel.SearchDogData
import com.bumptech.glide.Glide

class SearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val searchesList = mutableListOf<SearchDogData>()
    private val recentList = mutableListOf<String>()

    interface ItemClick {
        fun onSearchClick(item: SearchDogData)
        fun onImageViewClick(position: Int)
        fun onTextViewClick(position: Int)
    }

    var itemClick: ItemClick? = null

    override fun getItemViewType(position: Int): Int {
        return typeOne
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val binding = ItemCommonDetailBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SearchHolder(binding)
            }

            1 -> {
                val binding = ItemRecyclerViewRecentWordBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RecentWordHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (typeOne) {
            0 -> {
                val currentItem = searchesList[position]
                val searchHolder = holder as SearchHolder

                searchHolder.itemView.setOnClickListener {
                    val adapterPosition = searchHolder.bindingAdapterPosition
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        searchesList.getOrNull(adapterPosition)?.let { item ->
                            itemClick?.onSearchClick(item)
                        }
                    }
                }

                val address = currentItem.careAddr
                val parts = address?.split(" ")
                val result = "#${parts?.getOrNull(0)} ${parts?.getOrNull(1)}"

                Glide.with(searchHolder.itemView.context)
                    .load(currentItem.popfile)
                    .into(searchHolder.image)

                val kindText = currentItem.kindCd
                    ?.replace("[\uAC1C] ", "")
                    ?.replace("[\uAC1C]", "")
                    ?.trim()
                    .orEmpty()
                searchHolder.dogKind.text = kindText

                var text = "#${currentItem.age}"
                text += result
                text += "#${currentItem.processState}"
                text += when (currentItem.sexCd) {
                    "M" -> "#\uC218\uCEF7"
                    "F" -> "#\uC554\uCEF7"
                    else -> "#\uBBF8\uC0C1"
                }
                text += when (currentItem.neuterYn) {
                    "Y" -> "#\uC911\uC131\uD654"
                    "N" -> ""
                    else -> "#\uBBF8\uC0C1"
                }
                text += "#${currentItem.weight}"
                text += "\n#${currentItem.specialMark}"
                searchHolder.age.text = text
            }

            1 -> {
                val recentWordHolder = holder as RecentWordHolder
                recentWordHolder.recentText.text = recentList[position]
            }
        }
    }

    override fun getItemCount(): Int {
        return when (typeOne) {
            0 -> searchesList.size
            1 -> recentList.size
            else -> 0
        }
    }

    inner class SearchHolder(binding: ItemCommonDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val image = binding.dogImg
        val dogKind = binding.dogName
        val age = binding.dogTag
    }

    inner class RecentWordHolder(binding: ItemRecyclerViewRecentWordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val recentText = binding.recentText
        val cancel = binding.recentCancel

        init {
            cancel.setOnClickListener {
                val adapterPosition = bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemClick?.onImageViewClick(adapterPosition)
                }
            }
            recentText.setOnClickListener {
                val adapterPosition = bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemClick?.onTextViewClick(adapterPosition)
                }
            }
        }
    }

    fun searchesData(list: List<SearchDogData>) {
        searchesList.clear()
        searchesList.addAll(list)
        notifyDataSetChanged()
    }

    fun recentData(list: List<String>) {
        recentList.clear()
        recentList.addAll(list)
        notifyDataSetChanged()
    }

    companion object {
        var typeOne = 1
    }
}
