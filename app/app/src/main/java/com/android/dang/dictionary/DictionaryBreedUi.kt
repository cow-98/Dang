package com.android.dang.dictionary

import android.content.Context
import com.android.dang.R
import com.android.dang.dictionary.data.BreedsData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

private object DictionaryJsonLoader {
    private val gson = Gson()

    fun loadMap(context: Context, resId: Int): Map<String, String> {
        return runCatching {
            context.resources.openRawResource(resId)
                .bufferedReader(Charsets.UTF_8)
                .use { reader ->
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    gson.fromJson<Map<String, String>>(reader, type) ?: emptyMap()
                }
        }.getOrDefault(emptyMap())
    }
}

object BreedNameLocalizer {
    @Volatile
    private var cachedNames: Map<String, String>? = null

    fun localize(context: Context, englishName: String?): String {
        val fallback = englishName.orEmpty().trim()
        if (fallback.isBlank()) {
            return ""
        }

        val names = cachedNames ?: synchronized(this) {
            cachedNames ?: DictionaryJsonLoader.loadMap(context, R.raw.dictionary_breed_names_ko)
                .also { cachedNames = it }
        }
        return names[fallback].orEmpty().ifBlank { fallback }
    }
}

private object DictionaryValueLocalizer {
    @Volatile
    private var cachedTemperaments: Map<String, String>? = null

    @Volatile
    private var cachedOrigins: Map<String, String>? = null

    @Volatile
    private var cachedDescriptions: Map<String, String>? = null

    @Volatile
    private var cachedHistories: Map<String, String>? = null

    @Volatile
    private var cachedPurposes: Map<String, String>? = null

    fun localizeTemperament(context: Context, value: String?): String {
        val parts = value.orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (parts.isEmpty()) {
            return ""
        }

        val map = cachedTemperaments ?: synchronized(this) {
            cachedTemperaments ?: loadCaseInsensitiveMap(
                DictionaryJsonLoader.loadMap(context, R.raw.dictionary_temperament_ko)
            ).also { cachedTemperaments = it }
        }
        return parts.joinToString(", ") { token ->
            map[token] ?: map[token.lowercase(Locale.US)] ?: token
        }
    }

    fun localizeOrigin(context: Context, value: String?): String {
        val origin = value.orEmpty().trim()
        if (origin.isBlank()) {
            return ""
        }

        val map = cachedOrigins ?: synchronized(this) {
            cachedOrigins ?: DictionaryJsonLoader.loadMap(context, R.raw.dictionary_origin_ko)
                .also { cachedOrigins = it }
        }
        return map[origin].orEmpty().ifBlank { origin }
    }

    fun localizeDescription(context: Context, item: BreedsData.BreedsDataItem): String {
        return localizedText(
            context = context,
            item = item,
            cache = cachedDescriptions,
            resId = R.raw.dictionary_descriptions_ko,
            assignCache = { cachedDescriptions = it }
        ).ifBlank { item.description.orEmpty().trim() }
    }

    fun localizeHistory(context: Context, item: BreedsData.BreedsDataItem): String {
        return localizedText(
            context = context,
            item = item,
            cache = cachedHistories,
            resId = R.raw.dictionary_histories_ko,
            assignCache = { cachedHistories = it }
        ).ifBlank { item.history.orEmpty().trim() }
    }

    fun localizePurpose(context: Context, item: BreedsData.BreedsDataItem): String {
        return localizedText(
            context = context,
            item = item,
            cache = cachedPurposes,
            resId = R.raw.dictionary_purposes_ko,
            assignCache = { cachedPurposes = it }
        ).ifBlank { item.bred_for.orEmpty().trim() }
    }

    private fun localizedText(
        context: Context,
        item: BreedsData.BreedsDataItem,
        cache: Map<String, String>?,
        resId: Int,
        assignCache: (Map<String, String>) -> Unit
    ): String {
        val breedId = item.id?.toString().orEmpty()
        if (breedId.isBlank()) {
            return ""
        }

        val map = cache ?: synchronized(this) {
            cache ?: DictionaryJsonLoader.loadMap(context, resId)
                .also(assignCache)
        }
        return map[breedId].orEmpty().trim()
    }

    private fun loadCaseInsensitiveMap(source: Map<String, String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        source.forEach { (key, value) ->
            val normalizedKey = key.trim()
            val normalizedValue = value.trim()
            if (normalizedKey.isNotBlank() && normalizedValue.isNotBlank()) {
                result[normalizedKey] = normalizedValue
                result[normalizedKey.lowercase(Locale.US)] = normalizedValue
            }
        }
        return result
    }
}

object DictionaryBreedUi {
    private const val LABEL_LIFE_SPAN = "\uC218\uBA85"
    private const val LABEL_BREED_GROUP = "\uBD84\uB958"
    private const val LABEL_COUNTRY = "\uAD6D\uAC00"
    private const val LABEL_TEMPERAMENT = "\uC131\uACA9"
    private const val LABEL_WEIGHT = "\uBAB8\uBB34\uAC8C"
    private const val LABEL_HEIGHT = "\uD0A4"
    private const val LABEL_PURPOSE = "\uC6A9\uB3C4"

    private val breedGroupNames = mapOf(
        "Herding" to "\uD5C8\uB529",
        "Hound" to "\uD558\uC6B4\uB4DC",
        "Mixed" to "\uBBF9\uC2A4",
        "Non-Sporting" to "\uB17C\uC2A4\uD3EC\uD305",
        "Sporting" to "\uC2A4\uD3EC\uD305",
        "Terrier" to "\uD14C\uB9AC\uC5B4",
        "Toy" to "\uD1A0\uC774",
        "Utility" to "\uC720\uD2F8\uB9AC\uD2F0",
        "Working" to "\uC6CC\uD0B9",
        "Guardian" to "\uAC00\uB514\uC5B8",
        "Companion" to "\uCEF4\uD328\uB2C8\uC5B8"
    )

    fun imageUrl(item: BreedsData.BreedsDataItem): String? {
        return item.image?.url?.takeIf { it.isNotBlank() }
            ?: item.reference_image_id?.takeIf { it.isNotBlank() }
                ?.let { "https://cdn2.thedogapi.com/images/$it.jpg" }
    }

    fun cardSummary(context: Context, item: BreedsData.BreedsDataItem): String {
        val lines = mutableListOf<String>()

        formatLifeSpan(item.life_span)?.let {
            lines += "$LABEL_LIFE_SPAN : $it"
        }

        val groupAndCountry = listOfNotNull(
            localizeBreedGroup(item.breed_group).takeIf { it.isNotBlank() }
                ?.let { "$LABEL_BREED_GROUP : $it" },
            localizeCountry(context, item).takeIf { it.isNotBlank() }
                ?.let { "$LABEL_COUNTRY : $it" }
        ).joinToString(" | ")
        if (groupAndCountry.isNotBlank()) {
            lines += groupAndCountry
        }

        DictionaryValueLocalizer.localizeTemperament(context, item.temperament)
            .takeIf { it.isNotBlank() }
            ?.let { lines += "$LABEL_TEMPERAMENT : ${trimWithEllipsis(it, 52)}" }

        if (lines.isEmpty()) {
            description(context, item).takeIf { it.isNotBlank() }?.let {
                lines += trimWithEllipsis(it, 80)
            }
        }

        return lines.joinToString("\n")
    }

    fun detailInfo(context: Context, item: BreedsData.BreedsDataItem): String {
        val facts = mutableListOf<String>()
        appendFact(facts, LABEL_BREED_GROUP, localizeBreedGroup(item.breed_group))
        appendFact(facts, LABEL_COUNTRY, localizeCountry(context, item))
        appendFact(facts, LABEL_LIFE_SPAN, formatLifeSpan(item.life_span))
        appendFact(facts, LABEL_WEIGHT, formatMetric(item.weight?.metric, "kg"))
        appendFact(facts, LABEL_HEIGHT, formatMetric(item.height?.metric, "cm"))
        appendFact(
            facts,
            LABEL_TEMPERAMENT,
            DictionaryValueLocalizer.localizeTemperament(context, item.temperament)
        )
        appendFact(
            facts,
            LABEL_PURPOSE,
            normalizeValue(DictionaryValueLocalizer.localizePurpose(context, item)).ifBlank { null }
        )
        return facts.joinToString("\n")
    }

    fun description(context: Context, item: BreedsData.BreedsDataItem): String {
        return normalizeParagraph(DictionaryValueLocalizer.localizeDescription(context, item))
            .ifBlank { normalizeValue(DictionaryValueLocalizer.localizePurpose(context, item)) }
    }

    fun history(context: Context, item: BreedsData.BreedsDataItem): String {
        return normalizeParagraph(DictionaryValueLocalizer.localizeHistory(context, item))
    }

    private fun appendFact(facts: MutableList<String>, label: String, value: String?) {
        val text = value.orEmpty().trim()
        if (text.isNotBlank()) {
            facts += "$label : $text"
        }
    }

    private fun localizeBreedGroup(value: String?): String {
        val text = value.orEmpty().trim()
        return breedGroupNames[text] ?: text
    }

    private fun localizeCountry(context: Context, item: BreedsData.BreedsDataItem): String {
        val countryCode = item.country_code.orEmpty().trim()
        if (countryCode.isNotBlank()) {
            val localized = Locale("", countryCode.uppercase(Locale.US))
                .getDisplayCountry(Locale.KOREAN)
                .trim()
            if (localized.isNotBlank() && !localized.equals(countryCode, ignoreCase = true)) {
                return localized
            }
        }

        return DictionaryValueLocalizer.localizeOrigin(context, item.origin)
    }

    private fun formatLifeSpan(value: String?): String? {
        val normalized = normalizeRange(value)
        return if (normalized.isBlank()) {
            null
        } else {
            "${normalized}\uC138"
        }
    }

    private fun formatMetric(value: String?, unit: String): String? {
        val normalized = normalizeRange(value)
        return if (normalized.isBlank()) {
            null
        } else {
            "$normalized $unit"
        }
    }

    private fun normalizeRange(value: String?): String {
        return value.orEmpty()
            .trim()
            .replace("-", " - ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun normalizeValue(value: String): String {
        return value
            .replace(";", "; ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun normalizeParagraph(value: String): String {
        return value
            .replace(Regex("[\\t\\r\\n]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun trimWithEllipsis(value: String, maxLength: Int): String {
        return if (value.length > maxLength) {
            value.take(maxLength - 3) + "..."
        } else {
            value
        }
    }
}
