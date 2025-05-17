package com.example.skinnote

import androidx.room.Entity
import androidx.room.PrimaryKey

// creates table for spinner skin products
@Entity(tableName = "products")
data class ProductsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,       // product name
    val type: String        // whether its face wash, cleanser etc
)