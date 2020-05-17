package com.unlam.soa.sharedPreferences

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "FitSOA"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    //SharedPreferences variables
    private val IS_LOGGED = Pair("is_logged", false)
    private val TOKEN = Pair("token", "")

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    //an inline function to put variable and save it
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    //SharedPreferences variables getters/setters
    var isLogged: Boolean
        get() = preferences.getBoolean(IS_LOGGED.first, IS_LOGGED.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_LOGGED.first, value)
        }

    var token: String
        get() = preferences.getString(TOKEN.first, TOKEN.second) ?: ""
        set(value) = preferences.edit {
            it.putString(TOKEN.first, value)
        }
}