package com.android.dang.dictionary.retrofit

import com.android.dang.dictionary.data.BreedImageSearchItem
import com.android.dang.dictionary.data.BreedsData
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface NetWorkInterface {
    @GET("/v1/breeds")
    suspend fun getBreeds(
        @Header("x-api-key") token: String?,
        @QueryMap param: HashMap<String, Int>
    ): BreedsData

    @GET("/v1/images/search")
    suspend fun getBreedImages(
        @Header("x-api-key") token: String?,
        @Query("breed_id") breedId: String,
        @Query("limit") limit: Int = 3,
        @Query("has_breeds") hasBreeds: Boolean = true,
        @Query("include_breeds") includeBreeds: Boolean = true
    ): List<BreedImageSearchItem>
}
