package com.example.todobase.data.models

data class FavoritePlace(
    val uid:String = "",
    val name:String = "",
    val description:String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
