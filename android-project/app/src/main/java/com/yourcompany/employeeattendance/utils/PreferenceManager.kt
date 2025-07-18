package com.yourcompany.employeeattendance.utils

import android.content.Context
import android.content.SharedPreferences
import com.yourcompany.employeeattendance.models.User

class PreferenceManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "EmployeeAttendancePrefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NAME = "name"
        private const val KEY_CLUSTER = "cluster"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_CREATED_AT = "created_at"
    }
    
    fun saveUser(user: User) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, user.id)
        editor.putString(KEY_USERNAME, user.username)
        editor.putString(KEY_NAME, user.name)
        editor.putString(KEY_CLUSTER, user.cluster)
        editor.putString(KEY_PHONE_NUMBER, user.phoneNumber)
        editor.putString(KEY_CREATED_AT, user.createdAt)
        editor.apply()
    }
    
    fun getUser(): User? {
        return if (isLoggedIn()) {
            User(
                id = sharedPreferences.getInt(KEY_USER_ID, 0),
                username = sharedPreferences.getString(KEY_USERNAME, "") ?: "",
                password = "", // Don't store password locally
                name = sharedPreferences.getString(KEY_NAME, "") ?: "",
                cluster = sharedPreferences.getString(KEY_CLUSTER, "") ?: "",
                phoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: "",
                createdAt = sharedPreferences.getString(KEY_CREATED_AT, "") ?: ""
            )
        } else {
            null
        }
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, 0)
    }
    
    fun getUsername(): String {
        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
    }
    
    fun getName(): String {
        return sharedPreferences.getString(KEY_NAME, "") ?: ""
    }
    
    fun getCluster(): String {
        return sharedPreferences.getString(KEY_CLUSTER, "") ?: ""
    }
    
    fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
