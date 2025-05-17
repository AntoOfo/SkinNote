package com.example.skinnote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// how room will interact w db
@Dao
interface SkinNoteDao {

    // adds new entry to db
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: SkinEntry)

    // sorts all entries by time
    @Query("SELECT * FROM skincare_entries ORDER BY timestamp DESC")
    suspend fun getAllEntries(): List<SkinEntry>
}