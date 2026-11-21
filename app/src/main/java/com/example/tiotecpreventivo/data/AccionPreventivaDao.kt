package com.example.tiotecpreventivo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AccionPreventivaDao {

    @Query("SELECT * FROM acciones_preventivas ORDER BY id")
    suspend fun obtenerTodas(): List<AccionPreventiva>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(accion: AccionPreventiva)

    @Query("SELECT * FROM acciones_preventivas WHERE pendienteDeSincronizar = 1 ORDER BY id")
    suspend fun obtenerPendientes(): List<AccionPreventiva>

    @Query("SELECT * FROM acciones_preventivas WHERE id = :id")
    suspend fun obtener(id: Int): AccionPreventiva?

    @Query("DELETE FROM acciones_preventivas WHERE id IN (:ids)")
    suspend fun eliminarPorIds(ids: List<Int>)

    @Query("""
        UPDATE acciones_preventivas SET pendienteDeSincronizar = 0
        WHERE id = :id AND completada = :completada
    """)
    suspend fun confirmarSincronizacion(id: Int, completada: Boolean)

    @Transaction
    suspend fun guardarDesdeServidor(acciones: List<AccionPreventiva>) {
        for (accion in acciones) {
            if (obtener(accion.id)?.pendienteDeSincronizar != true) {
                guardar(accion)
            }
        }
    }
}
