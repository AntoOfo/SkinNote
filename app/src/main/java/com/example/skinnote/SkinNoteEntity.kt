package com.example.skinnote

import androidx.room.Entity
import androidx.room.PrimaryKey

// creates table
@Entity(tableName = "skincare_entries")
data class SkinEntry(       // each row is a whole entry
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var faceWash: String,
    var cleanser: String,
    var serum: String,
    var moisturiser: String,
    var skinFeel: Int?,
    // for recycler view maybe
    val timestamp: Long = System.currentTimeMillis(),
    val selfieUri: String? = null
)