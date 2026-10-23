package com.example.tiotecpreventivo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "acciones_preventivas")
data class AccionPreventiva(
    @PrimaryKey val id: Int,
    val completada: Boolean
)