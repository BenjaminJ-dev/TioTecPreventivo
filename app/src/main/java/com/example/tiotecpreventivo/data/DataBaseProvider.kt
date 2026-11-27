package com.example.tiotecpreventivo.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    private val migracionChecklist = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE acciones_preventivas ADD COLUMN pendienteDeSincronizar INTEGER NOT NULL DEFAULT 1"
            )
        }
    }

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun obtenerBaseDeDatos(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instancia = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tiotec_database"
            ).addMigrations(migracionChecklist).build()

            INSTANCE = instancia
            instancia
        }
    }
}
