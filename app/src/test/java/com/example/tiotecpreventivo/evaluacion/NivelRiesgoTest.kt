package com.example.tiotecpreventivo.evaluacion

import org.junit.Assert.assertEquals
import org.junit.Test

class NivelRiesgoTest {
    @Test
    fun ceroRespuestasNegativasEsNulo() {
        assertEquals("Nulo", calcularNivelRiesgo(listOf(true, true, true, true)))
    }

    @Test
    fun unaRespuestaNegativaEsBajo() {
        assertEquals("Bajo", calcularNivelRiesgo(listOf(true, false, true, true)))
    }

    @Test
    fun dosRespuestasNegativasEsMedio() {
        assertEquals("Medio", calcularNivelRiesgo(listOf(false, true, false, true)))
    }

    @Test
    fun tresRespuestasNegativasEsAlto() {
        assertEquals("Alto", calcularNivelRiesgo(listOf(false, false, true, false)))
    }

    @Test
    fun cuatroRespuestasNegativasEsAlto() {
        assertEquals("Alto", calcularNivelRiesgo(listOf(false, false, false, false)))
    }
}
