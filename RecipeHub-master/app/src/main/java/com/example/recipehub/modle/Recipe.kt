package com.example.recipehub.modle

data class Recipe(
    val _id: String?, // MongoDB ObjectId as a String
    val userId: String?, // Reference to the User
    val title: String,
    val image: String?,
    val description: String?,
    val ingredients: String,
    val createdAt: String?, // Timestamp for creation
    val updatedAt: String? // Timestamp for last update
)