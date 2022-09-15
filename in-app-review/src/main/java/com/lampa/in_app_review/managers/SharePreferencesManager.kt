package com.lampa.in_app_review.managers

import android.content.Context
import android.content.SharedPreferences

class SharePreferencesManager(appContext: Context) {

    companion object {
        private const val SHARED_PREFERENCES_NAME = "BaseSharedPreferences"

        const val OPENED_APP_TIMES = "OPENED_APP_TIMES"
        const val IS_RATING_FLOW_FINISHED = "IS_RATING_FLOW_FINISHED"
        const val SHOULD_START_RATING_FLOW = "SHOULD_START_RATING_FLOW"
    }

    private val preferences = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = preferences.edit()

    var openedAppTimes: Int
        get() = preferences.getInt(OPENED_APP_TIMES, 0)
        set(value) {
            editor.putInt(OPENED_APP_TIMES, value)
            editor.apply()
        }

    var isRatingFlowFinished: Boolean
        get() = preferences.getBoolean(IS_RATING_FLOW_FINISHED, false)
        set(value) {
            editor.putBoolean(IS_RATING_FLOW_FINISHED, value)
            editor.apply()
        }

    var shouldStartRatingFlow: Boolean
        get() = preferences.getBoolean(SHOULD_START_RATING_FLOW, false)
        set(value) {
            editor.putBoolean(SHOULD_START_RATING_FLOW, value)
            editor.apply()
        }
}