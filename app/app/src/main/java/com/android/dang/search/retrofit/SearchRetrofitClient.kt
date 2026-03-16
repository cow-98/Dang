package com.android.dang.search.retrofit

import com.android.dang.home.retrofit.HomeData
import com.android.dang.home.retrofit.Util
import com.android.dang.retrofit.kind.Kind
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {
    @GET("kind")
    fun kindSearch(
        @Query("serviceKey") serviceKey: String = Util.KEY,
        @Query("up_kind_cd") kind: Int = 417000,
        @Query("_type") type: String = "json"
    ): retrofit2.Call<Kind?>

    @GET("abandonmentPublic_v2")
    fun abandonedDogSearch(
        @Query("serviceKey") serviceKey: String = Util.KEY,
        @Query("upkind") upkind: Int = 417000,
        @Query("_type") type: String = "json",
        @Query("numOfRows") numOfRows: Int,
        @Query("kind") kind: String
    ): retrofit2.Call<HomeData?>
}

object SearchRetrofitClient {
    private val gson by lazy {
        GsonBuilder().setLenient().create()
    }

    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Util.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    val apiService: SearchApiService by lazy {
        instance.create(SearchApiService::class.java)
    }
}
