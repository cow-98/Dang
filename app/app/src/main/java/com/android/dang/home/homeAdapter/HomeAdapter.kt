package com.android.dang.home.homeAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.dang.R
import com.android.dang.databinding.ItemCommonDetailBinding
import com.android.dang.search.searchItemModel.SearchDogData
import com.android.dang.util.PrefManager.addItem
import com.android.dang.util.PrefManager.deleteItem
import com.bumptech.glide.Glide

class HomeAdapter(private val mContext: Context) :
    RecyclerView.Adapter<HomeAdapter.ItemViewHolder>() {

    interface ItemClick {
        fun onClick(position: Int)
    }

    var itemClick: ItemClick? = null
    private val items = ArrayList<SearchDogData>()

    fun clearItem() {
        items.clear()
    }

    fun addItem(items2: List<SearchDogData>) {
        clearItem()
        items.addAll(items2)
        notifyDataSetChanged()
    }

    private fun ellipsizeText(
        age: String?,
        specialMark: String?,
        careAddr: String?,
        processState: String?,
        maxLength: Int
    ): String {
        val ellipstext =
            "#${age ?: ""} #${specialMark ?: ""} ${careAddr ?: ""} #${processState ?: ""}"
        return ellipstext.ellipsize(maxLength)
    }

    private fun String.ellipsize(maxLength: Int): String {
        return if (length > maxLength) {
            val halfLength = maxLength / 2
            "${substring(0, halfLength)}...${substring(length - halfLength)}"
        } else {
            this
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemCommonDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]

        holder.itemView.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                itemClick?.onClick(adapterPosition)
            }
        }

        val address = currentItem.careAddr
        val parts = address?.split(" ")
        val result = "#${parts?.getOrNull(0)} ${parts?.getOrNull(1)}"

        Glide.with(mContext)
            .load(currentItem.popfile)
            .into(holder.dogImg)

        val modifiedKindCd = currentItem.kindCd?.replace("[개]", "")?.trim() ?: ""
        holder.dogName.text = modifiedKindCd

        holder.dogTag.text = ellipsizeText(
            currentItem.age,
            currentItem.specialMark,
            result,
            currentItem.processState,
            70
        )

        holder.dogLike.setImageResource(
            if (currentItem.isLiked) R.drawable.icon_like_on else R.drawable.icon_like_off
        )
        holder.dogLike.setOnClickListener {
            currentItem.isLiked = !currentItem.isLiked
            if (currentItem.isLiked) {
                holder.dogLike.setImageResource(R.drawable.icon_like_on)
                addItem(mContext, currentItem)
            } else {
                holder.dogLike.setImageResource(R.drawable.icon_like_off)
                currentItem.popfile?.let { popfile ->
                    deleteItem(mContext, popfile)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(binding: ItemCommonDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val dogImg: ImageView = binding.dogImg
        val dogName: TextView = binding.dogName
        val dogTag: TextView = binding.dogTag
        val dogLike: ImageView = binding.dogLike
        val dogBox: ConstraintLayout = binding.dogBox
    }
}
