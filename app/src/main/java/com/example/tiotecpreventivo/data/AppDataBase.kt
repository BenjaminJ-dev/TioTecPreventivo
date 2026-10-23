package com.example.tiotecpreventivo.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AccionPreventiva::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accionPreventivaDao(): AccionPreventivaDao
}