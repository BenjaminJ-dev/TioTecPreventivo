package com.example.tiotecpreventivo.evaluacion

fun calcularNivelRiesgo(respuestas: List<Boolean?>): String {
    return when (respuestas.count { it == false }) {
        0 -> "Nulo"
        1 -> "Bajo"
        2 -> "Medio"
        else -> "Alto"
    }
}
