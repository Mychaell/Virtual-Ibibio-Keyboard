package com.example.virtualkeyboard

import android.content.Context
import android.content.SharedPreferences

internal class PrefManager(private val context: Context) {

    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = pref.edit()
    }

    var isActivated: Boolean
        get() = pref.getBoolean(IS_ACTIVATED, false)
        set(value) {
            editor.putBoolean(IS_ACTIVATED, value)
            editor.apply()
        }

    var isAdded: Boolean
        get() = pref.getBoolean(IS_ADDED, false)
        set(value) {
            editor.putBoolean(IS_ADDED, value)
            editor.apply()
        }

    fun setIsActivated(value: Boolean) {
        isActivated = value
    }

    @JvmName("setAdded1")
    fun setAdded(value: Boolean) {
        isAdded = value
    }

    companion object {
        private const val PREF_NAME = "com.example.virtualkeyboard"
        private const val IS_ACTIVATED = "IsActivated"
        private const val IS_ADDED = "isAdded"
    }
}
