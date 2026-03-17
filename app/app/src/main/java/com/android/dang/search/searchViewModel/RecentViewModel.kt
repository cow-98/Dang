package com.android.dang.search.searchViewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentViewModel : ViewModel() {
    val recentList = MutableLiveData<List<String>>(emptyList())

    fun setRecentList(list: List<String>) {
        recentList.value = normalize(list)
    }

    fun recentAdd(text: String) {
        val keyword = text.trim()
        if (keyword.isBlank()) {
            return
        }

        val currentList = recentList.value.orEmpty().toMutableList()
        currentList.removeAll { it.equals(keyword, ignoreCase = true) }
        currentList.add(0, keyword)
        recentList.value = normalize(currentList)
    }

    fun recentRemove(position: Int) {
        val currentList = recentList.value.orEmpty().toMutableList()
        if (position !in currentList.indices) {
            return
        }
        currentList.removeAt(position)
        recentList.value = currentList
    }

    fun editText(position: Int): String {
        return recentList.value.orEmpty().getOrElse(position) { "" }
    }

    fun clearAll() {
        recentList.value = emptyList()
    }

    fun saveListToPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("recentWord", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(recentList.value.orEmpty())
        editor.putString("recentWord", json)
        editor.apply()
    }

    fun getListFromPreferences(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences("recentWord", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("recentWord", null)
        val type = object : TypeToken<List<String>>() {}.type
        val list = gson.fromJson<List<String>>(json, type) ?: emptyList()
        return normalize(list)
    }

    private fun normalize(source: List<String>): List<String> {
        val normalized = mutableListOf<String>()
        source.forEach { item ->
            val keyword = item.trim()
            if (keyword.isBlank()) {
                return@forEach
            }
            if (normalized.none { it.equals(keyword, ignoreCase = true) }) {
                normalized += keyword
            }
        }
        return normalized.take(20)
    }
}
