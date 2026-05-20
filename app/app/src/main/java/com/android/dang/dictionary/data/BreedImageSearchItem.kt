package com.android.dang.dictionary.data

data class BreedImageSearchItem(
    var id: String?,
    var url: String?,
    var width: Int?,
    var height: Int?,
    var breeds: List<BreedImageBreed>?
) {
    data class BreedImageBreed(
        var id: String?,
        var name: String?
    )
}
