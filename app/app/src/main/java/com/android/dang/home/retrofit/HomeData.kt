package com.android.dang.home.retrofit

import com.google.gson.annotations.SerializedName

data class HomeData(
    @SerializedName("response")
    val response: Response
)

data class Response(
    @SerializedName("header")
    val header: Header,
    @SerializedName("body")
    val body: Body
)

data class Header(
    @SerializedName("reqNo")
    val reqNo: Long?,
    @SerializedName("resultCode")
    val resultCode: String?,
    @SerializedName("resultMsg")
    val resultMsg: String?
)

data class Body(
    @SerializedName("items")
    val items: Items?,
    @SerializedName("numOfRows")
    val numOfRows: Int?,
    @SerializedName("pageNo")
    val pageNo: Int?,
    @SerializedName("totalCount")
    val totalCount: Int?
)

data class Items(
    @SerializedName("item")
    val item: List<Item>?
)

data class Item(
    @SerializedName("desertionNo")
    val desertionNo: String?,

    @SerializedName("happenDt")
    val happenDt: String?,

    @SerializedName("happenPlace")
    val happenPlace: String?,

    @SerializedName("kindFullNm")
    val kindFullNm: String?,

    @SerializedName("upKindCd")
    val upKindCd: String?,

    @SerializedName("upKindNm")
    val upKindNm: String?,

    @SerializedName("kindCd")
    val kindCd: String?,

    @SerializedName("kindNm")
    val kindNm: String?,

    @SerializedName("colorCd")
    val colorCd: String?,

    @SerializedName("age")
    val age: String?,

    @SerializedName("weight")
    val weight: String?,

    @SerializedName("noticeNo")
    val noticeNo: String?,

    @SerializedName("noticeSdt")
    val noticeSdt: String?,

    @SerializedName("noticeEdt")
    val noticeEdt: String?,

    @SerializedName("popfile1")
    val popfile1: String?,

    @SerializedName("popfile2")
    val popfile2: String?,

    @SerializedName("processState")
    val processState: String?,

    @SerializedName("sexCd")
    val sexCd: String?,

    @SerializedName("neuterYn")
    val neuterYn: String?,

    @SerializedName("specialMark")
    val specialMark: String?,

    @SerializedName("careRegNo")
    val careRegNo: String?,

    @SerializedName("careNm")
    val careNm: String?,

    @SerializedName("careTel")
    val careTel: String?,

    @SerializedName("careAddr")
    val careAddr: String?,

    @SerializedName("careOwnerNm")
    val careOwnerNm: String?,

    @SerializedName("orgNm")
    val orgNm: String?,

    @SerializedName("updTm")
    val updTm: String?
)