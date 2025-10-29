package com.nvd.demo_list.screens.memo

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class MemoRepository(private val context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("memo_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val MEMOS_KEY = "memos"
    }
    
    fun saveMemos(memos: List<Memo>) {
        val memosJson = gson.toJson(memos)
        sharedPreferences.edit()
            .putString(MEMOS_KEY, memosJson)
            .apply()
    }
    
    fun loadMemos(): List<Memo> {
        val memosJson = sharedPreferences.getString(MEMOS_KEY, null)
        return if (memosJson != null) {
            try {
                val type = object : TypeToken<List<Memo>>() {}.type
                gson.fromJson(memosJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun clearMemos() {
        sharedPreferences.edit()
            .remove(MEMOS_KEY)
            .apply()
    }
}
