package com.iplacex.listacompras

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseBuilder {
    private var INSTANCE: ShoppingDatabase? = null

    // Definición de la migración de la versión 1 a la 2
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE shopping_list_new (
                    id INTEGER PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    isPurchased INTEGER NOT NULL,
                    imageUrl TEXT
                )
            """.trimIndent())

            database.execSQL("""
                INSERT INTO shopping_list_new (id, name, isPurchased, imageUrl)
                SELECT id, name, isPurchased, NULL FROM shopping_list
            """.trimIndent())

            database.execSQL("DROP TABLE shopping_list")

            database.execSQL("ALTER TABLE shopping_list_new RENAME TO shopping_list")
        }
    }

    fun getInstance(context: Context): ShoppingDatabase {
        if (INSTANCE == null) {
            synchronized(ShoppingDatabase::class) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    ShoppingDatabase::class.java,
                    "shopping_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    // .fallbackToDestructiveMigration() // Descomentar esto si se desea hacer una migracion destructiva
                    .build()
            }
        }
        return INSTANCE!!
    }
}