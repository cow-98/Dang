package com.android.dang.home.homeAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.recyclerview.widget.RecyclerView
import com.android.dang.MainActivity
import com.android.dang.R
import com.android.dang.databinding.ItemCommonDetailBinding
import com.android.dang.home.HomeFragment
import com.android.dang.home.retrofit.HomeItemModel
import com.android.dang.search.searchItemModel.SearchDogData
import com.android.dang.util.PrefManager.addItem
import com.android.dang.util.PrefManager.deleteItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class HomeAdapter(private val mContext: Context) :
    RecyclerView.Adapter<HomeAdapter.ItemViewHolder>() {
    var items = ArrayList<SearchDogData>()

    fun clearItem() {
        items.clear()
    }
    fun addItem(items2: ArrayList<SearchDogData>){
        for (item in items2){
            Log.d("homeadapter", "addItem popfile = ${item.popfile} / isLike = ${item.isLiked}")
        }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.ItemViewHolder {
        val binding =
            ItemCommonDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]
        Log.d("homeadapter", "popfile = ${currentItem.popfile} / isLike = ${currentItem.isLiked}")

        val address = currentItem.careAddr
        val parts = address?.split(" ")
        val result = "#${parts?.get(0)} ${parts?.get(1)}"

        Glide.with(mContext)
            .load(currentItem.popfile)
            .into(holder.dogImg)
        val modifiedKindCd = currentItem.kindCd?.replace("[개]", "")?.trim() ?: ""
        holder.dogName.text = modifiedKindCd

        val processText = ellipsizeText(
            currentItem.age,
            currentItem.specialMark,
            result,
            currentItem.processState,
            70
        )
        holder.dogTag.text = processText


        if (currentItem.isLiked) {
            holder.dogLike.setImageResource(R.drawable.icon_like_on)
        } else {
            holder.dogLike.setImageResource(R.drawable.icon_like_off)
        }
        holder.dogLike.setOnClickListener {
            currentItem.isLiked = !currentItem.isLiked
            if (currentItem.isLiked) {
                holder.dogLike.setImageResource(R.drawable.icon_like_on)
                addItem(mContext, currentItem)
                items[position].isLiked = true
                Log.d("homeadapter", "like: $currentItem")
            } else {
                holder.dogLike.setImageResource(R.drawable.icon_like_off)
                val popfile = currentItem.popfile!!
                deleteItem(mContext, popfile)
                items[position].isLiked = false
                Log.e("homeadapter", "del: $currentItem")
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(binding: ItemCommonDetailBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        var dogImg: ImageView = binding.dogImg
        var dogName: TextView = binding.dogName
        var dogTag: TextView = binding.dogTag
        var dogLike: ImageView = binding.dogLike
        var dogBox: ConstraintLayout = binding.dogBox


        init {
//           dogImg.setOnClickListener(this)
//            dogBox.setOnClickListener(this)
            dogLike.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            Log.d("homeadapter", "like: onClick")
            view?.let {

            }
        }
    }
}
