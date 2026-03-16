package com.android.dangtheland.retrofit.kind

import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("kindCd")
    val kindCd: String,
    @SerializedName(value = "kindNm", alternate = ["knm"])
    val kindNm: String
)
