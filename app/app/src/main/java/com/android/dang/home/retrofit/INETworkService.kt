package com.android.dang.home.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface INETworkService {
    @GET("abandonmentPublic_v2")
    fun homeDang(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int = 1,
        @Query("_type") type: String = "json",
        @Query("upkind") upkind: Int
    ) : Call<HomeData?>
}