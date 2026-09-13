package com.example.todobase.data.models


data class User(
    val uid:String = "",
    val username:String = "",
    val email:String = "",
    val profilePictureUrl:String = "",
    val createdAt:Long = System.currentTimeMillis(), 
)
