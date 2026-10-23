package com.example.tiotecpreventivo.data

class AccionPreventivaRepository(
    private val dao: AccionPreventivaDao
) {
    suspend fun obtenerTodas(): List<AccionPreventiva> {
        return dao.obtenerTodas()
    }

    suspend fun guardar(accion: AccionPreventiva) {
        dao.guardar(accion)
    }
}