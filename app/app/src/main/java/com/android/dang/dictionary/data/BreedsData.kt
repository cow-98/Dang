package com.android.dang.dictionary.data

class BreedsData : ArrayList<BreedsData.BreedsDataItem>() {
    data class BreedsDataItem(
        var bred_for: String?,
        var breed_group: String?,
        var country_code: String?,
        var description: String?,
        var height: Height?,
        var history: String?,
        var id: Int?,
        var life_span: String?,
        var name: String?,
        var origin: String?,
        var reference_image_id: String?,
        var temperament: String?,
        var weight: Weight?,
        var image: Image?
    ) {
        data class Height(
            var imperial: String?,
            var metric: String?
        )

        data class Weight(
            var imperial: String?,
            var metric: String?
        )

        data class Image(
            var id: String?,
            var url: String?,
            var width: Int?,
            var height: Int?
        )
    }
}
