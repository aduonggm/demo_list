package com.nvd.demo_list.screens.memo

import com.google.gson.annotations.SerializedName
import java.util.Date
import java.util.UUID

data class Memo(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("content")
    val content: String,
    @SerializedName("createdAt")
    val createdAt: Date = Date(),
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false,
    @SerializedName("dueDate")
    val dueDate: Date? = null
)
