package com.example.todobase.data.models

data class User(
    val id: String = "",
    val username: String = "",
    val nama: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
