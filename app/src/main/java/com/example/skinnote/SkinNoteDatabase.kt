package com.example.skinnote

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// connecting both entities and dao, 1st entity
@Database(entities = [SkinEntry::class, ProductsEntry::class], version = 1)
abstract class SkinNoteDatabase : RoomDatabase() {

    // bringing in dao
    abstract fun skinNoteDao(): SkinNoteDao

    companion object {
        // single instance to prevent duplicate dbs
        @Volatile
        private var INSTANCE: SkinNoteDatabase? = null

        // either returns db or makes it if it doesnt exist
        fun getDatabase(context: Context): SkinNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkinNoteDatabase::class.java,
                    "SkinNoteDatabase"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}