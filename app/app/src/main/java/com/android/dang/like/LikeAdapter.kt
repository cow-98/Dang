package com.android.dang.like

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.android.dang.R
import com.android.dang.databinding.FragmentLikeBinding
import com.android.dang.databinding.ItemCommonDetailBinding
import com.android.dang.home.retrofit.HomeItemModel
import com.android.dang.search.searchItemModel.SearchDogData
import com.android.dang.util.PrefManager
import com.android.dang.util.PrefManager.addItem
import com.android.dang.util.PrefManager.deleteItem
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

class LikeAdapter(private val mContext: Context) :
    RecyclerView.Adapter<LikeAdapter.ItemViewHolder>() {
    var items = ArrayList<SearchDogData>()

    interface OnItemClickListener {
        fun onItemClick(item: SearchDogData, position: Int)
    }

    private var clickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeAdapter.ItemViewHolder {
        val binding =
            ItemCommonDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikeAdapter.ItemViewHolder, position: Int) {

        val currentItem = items[position]

        Glide.with(mContext)
            .load(currentItem.popfile)
            .into(holder.dogImg)
        val modifiedKindCd = currentItem.kindCd?.replace("[개]", "")?.trim() ?: ""
        holder.dogName.text = modifiedKindCd

        val processText = ellipsizeText(currentItem.age, currentItem.specialMark, currentItem.careAddr, currentItem.processState, 70)
        holder.dogTag.text = processText

        holder.itemView.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                clickListener?.onItemClick(items[adapterPosition], adapterPosition)
            }
        }

        holder.dogLike.setOnClickListener {
            // Like-list deletion is handled by swipe, so the heart icon does not navigate.
        }
        if (currentItem.isLiked) {
            holder.dogLike.setImageResource(R.drawable.icon_like_on)
        } else {
            holder.dogLike.setImageResource(R.drawable.icon_like_off)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(binding: ItemCommonDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var dogImg: ImageView = binding.dogImg
        var dogName: TextView = binding.dogName
        var dogTag: TextView = binding.dogTag
        var dogLike: ImageView = binding.dogLike
        var dogBox: ConstraintLayout = binding.dogBox

    }

    private fun ellipsizeText(age: String?, specialMark: String?, orgNm: String?, processState: String?, maxLength: Int): String{
        val ellipstext = "#${age ?: ""} #${specialMark ?: ""} #${orgNm ?: ""} #${processState ?: ""}"
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
    fun insertData(position: Int, item: SearchDogData) {
        items.add(position,item)
        notifyItemInserted(position)
        addItem(mContext,items[position])
    }

    fun data(position: Int) : SearchDogData{
        Log.d("item", "items.size: ${items.size}")
       return items[position]
    }

    fun removeData(position: Int) {
        try {
            items[position].popfile?.let { deleteItem(mContext, it) }
            items.removeAt(position)
            notifyItemRemoved(position)

        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }
}
