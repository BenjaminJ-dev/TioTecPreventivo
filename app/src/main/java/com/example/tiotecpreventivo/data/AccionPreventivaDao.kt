package com.example.tiotecpreventivo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AccionPreventivaDao {

    @Query("SELECT * FROM acciones_preventivas ORDER BY id")
    suspend fun obtenerTodas(): List<AccionPreventiva>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(accion: AccionPreventiva)
}