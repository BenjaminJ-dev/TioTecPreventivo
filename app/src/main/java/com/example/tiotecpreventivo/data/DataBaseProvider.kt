package com.example.tiotecpreventivo.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun obtenerBaseDeDatos(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instancia = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tiotec_database"
            ).build()

            INSTANCE = instancia
            instancia
        }
    }
}