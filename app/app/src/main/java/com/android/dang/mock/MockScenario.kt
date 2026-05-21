package com.android.dang.mock

import android.content.Intent
import com.android.dang.util.SharedPref

object MockScenario {
    const val EXTRA_SCENARIO = "dang_mock_scenario"
    const val NONE = ""
    const val SEARCH_EMPTY = "search_empty"
    const val SEARCH_NO_PHONE = "search_no_phone"
    const val SEARCH_RECENT_LIMIT = "search_recent_limit"

    private const val PREF_KEY = "DANG_MOCK_SCENARIO"

    fun applyFrom(intent: Intent?) {
        val scenario = intent?.getStringExtra(EXTRA_SCENARIO).orEmpty()
        SharedPref.setString(PREF_KEY, scenario)
    }

    fun current(): String {
        return SharedPref.getString(PREF_KEY, NONE)
    }

    fun isSearchEmpty(): Boolean {
        return current() == SEARCH_EMPTY
    }

    fun isSearchNoPhone(): Boolean {
        return current() == SEARCH_NO_PHONE
    }

    fun isSearchRecentLimit(): Boolean {
        return current() == SEARCH_RECENT_LIMIT
    }

    fun isEnabled(): Boolean {
        return current().isNotBlank()
    }
}
