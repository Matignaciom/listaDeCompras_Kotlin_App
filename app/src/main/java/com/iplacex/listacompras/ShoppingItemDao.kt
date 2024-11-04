package com.iplacex.listacompras

import androidx.room.*

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_list ORDER BY isPurchased ASC")
    suspend fun getAllItems(): List<ShoppingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Query("SELECT * FROM shopping_list WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): ShoppingItem
}