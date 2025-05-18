package com.example.skinnote

import androidx.room.Entity
import androidx.room.PrimaryKey

// creates table
@Entity(tableName = "skincare_entries")
data class SkinEntry(       // each row is a whole entry
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val faceWash: String,
    val cleanser: String,
    val serum: String,
    val moisturiser: String,
    val skinFeel: Int,
    // for recycler view maybe
    val timestamp: Long = System.currentTimeMillis(),
    val selfieUri: String? = null
)