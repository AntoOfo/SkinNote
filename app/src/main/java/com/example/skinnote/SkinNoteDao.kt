package com.example.skinnote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// how room will interact w db
@Dao
interface SkinNoteDao {

    // adds new entry to user choice db
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: SkinEntry)

    // sorts all entries by time
    @Query("SELECT * FROM skincare_entries ORDER BY timestamp DESC")
    suspend fun getAllEntries(): List<SkinEntry>

    // update an entry
    @androidx.room.Update
    suspend fun updateEntry(entry: SkinEntry)

    // delete an entry
    @androidx.room.Delete
    suspend fun deleteEntry(entry: SkinEntry)

    @androidx.room.Delete
    suspend fun deleteProduct(product: ProductsEntry)

    // adds new products to products db (from dialog)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductsEntry)

    // groups products of a certain type
    @Query("SELECT * FROM products WHERE type = :type")
    suspend fun getProductsByType(type: String): List<ProductsEntry>

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductsEntry>

}