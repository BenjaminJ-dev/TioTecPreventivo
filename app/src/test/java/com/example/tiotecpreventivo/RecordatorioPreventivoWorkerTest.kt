package com.example.tiotecpreventivo

import com.example.tiotecpreventivo.data.remote.AccionChecklistRemota
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordatorioPreventivoWorkerTest {
    @Test
    fun ignoraAccionesQueNoSonCasillasDelResultado() {
        assertFalse(RecordatorioPreventivoWorker.hayPendientes(
            intArrayOf(1), listOf(AccionChecklistRemota(0, false), AccionChecklistRemota(1, true))
        ))
    }

    @Test
    fun avisaConUnaAccionIncompletaOUnaRecomendacionSinMarcar() {
        assertTrue(RecordatorioPreventivoWorker.hayPendientes(intArrayOf(1), listOf(AccionChecklistRemota(1, false))))
        assertTrue(RecordatorioPreventivoWorker.hayPendientes(intArrayOf(1), emptyList()))
        assertTrue(RecordatorioPreventivoWorker.hayPendientes(
            intArrayOf(0, 1, 2), listOf(AccionChecklistRemota(0, true), AccionChecklistRemota(1, false))
        ))
    }

    @Test
    fun guardaSilencioSiTodoEstaCompleto() {
        assertFalse(RecordatorioPreventivoWorker.hayPendientes(
            intArrayOf(1, 2), listOf(AccionChecklistRemota(1, true), AccionChecklistRemota(2, true))
        ))
        assertFalse(RecordatorioPreventivoWorker.hayPendientes(intArrayOf(), emptyList()))
    }
}
