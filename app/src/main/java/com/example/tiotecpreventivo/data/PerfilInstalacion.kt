package com.example.tiotecpreventivo.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import java.io.File
import java.io.IOException
import java.util.UUID

object PerfilInstalacion {
    @Synchronized
    fun obtener(contexto: Context): String {
        val preferencias = contexto.getSharedPreferences("perfil_instalacion", Context.MODE_PRIVATE)
        val baseAnterior = contexto.noBackupFilesDir.resolve("tiotec_evaluacion.db")
        val uuid = preferencias.getString("uuid", null) ?: run {
            val perfil = leerUuidAnterior(baseAnterior) ?: UUID.randomUUID().toString()
            if (!preferencias.edit().putString("uuid", perfil).commit()) {
                throw IOException("No se pudo guardar el UUID de la instalación.")
            }
            perfil
        }
        if (baseAnterior.exists()) SQLiteDatabase.deleteDatabase(baseAnterior)
        return uuid
    }

    private fun leerUuidAnterior(archivo: File): String? {
        if (!archivo.exists()) return null
        return try {
            SQLiteDatabase.openDatabase(archivo.path, null, SQLiteDatabase.OPEN_READONLY).use { base ->
                base.rawQuery("SELECT uuid FROM instalacion WHERE id = 0", null).use { filas ->
                    if (filas.moveToFirst()) filas.getString(0) else null
                }
            }
        } catch (_: SQLiteException) {
            null
        }
    }
}
