package com.android.dang.dictionary

import com.android.dang.dictionary.data.BreedImageSearchItem
import com.android.dang.dictionary.data.BreedsData
import com.android.dang.dictionary.retrofit.NetWorkClient
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

object BreedImageRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cachedImageUrls = ConcurrentHashMap<Int, String>()
    private val missingBreedIds = ConcurrentHashMap.newKeySet<Int>()
    private val inFlightRequests = ConcurrentHashMap<Int, Deferred<String?>>()

    suspend fun resolveImageUrl(item: BreedsData.BreedsDataItem): String? {
        DictionaryBreedUi.imageUrl(item)?.let { return it }
        val breedId = item.id ?: return null
        return resolveImageUrl(breedId)
    }

    suspend fun resolveImageUrl(breedId: Int): String? {
        cachedImageUrls[breedId]?.let { return it }
        if (missingBreedIds.contains(breedId)) {
            return null
        }

        val deferred = inFlightRequests[breedId] ?: repositoryScope.async {
            val imageUrl = runCatching {
                NetWorkClient.dogNetWork.getBreedImages(
                    token = NetWorkClient.API_AUTHKEY,
                    breedId = breedId.toString()
                ).firstResolvableUrl(breedId)
            }.getOrNull()

            imageUrl?.also {
                cachedImageUrls[breedId] = it
            } ?: run {
                missingBreedIds += breedId
                null
            }
        }.also { created ->
            val existing = inFlightRequests.putIfAbsent(breedId, created)
            if (existing != null) {
                created.cancel()
            } else {
                created.invokeOnCompletion {
                    inFlightRequests.remove(breedId, created)
                }
            }
        }

        return (inFlightRequests[breedId] ?: deferred).await()
    }

    private fun List<BreedImageSearchItem>.firstResolvableUrl(breedId: Int): String? {
        val requestedBreedId = breedId.toString()
        return firstOrNull { item ->
            item.breeds.orEmpty().any { breed ->
                breed.id.orEmpty().trim() == requestedBreedId
            }
        }?.url?.trim()?.takeIf { it.isNotBlank() }
    }
}
