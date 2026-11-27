package com.example.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tiotecpreventivo.evaluacion.calcularNivelRiesgo

@Composable
fun ResultadoTioTec(
    modifier: Modifier = Modifier,
    preguntas: List<String>,
    respuestas: List<Boolean?>,
    accionesCompletadas: List<Boolean>,
    reiniciandoProgreso: Boolean,
    errorReinicio: String?,
    alReiniciarProgreso: () -> Unit,
    alCambiarAccion: (Int, Boolean) -> Unit,
    alVolver: () -> Unit
) {
    var confirmarReinicio by rememberSaveable { mutableStateOf(false) }
    if (confirmarReinicio) {
        AlertDialog(
            onDismissRequest = { confirmarReinicio = false },
            title = { Text("¿Reiniciar progreso?") },
            text = { Text("Se borrarán las marcas de todas las recomendaciones.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarReinicio = false
                    alReiniciarProgreso()
                }) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarReinicio = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val recomendaciones = listOf(
        "Activa una copia de seguridad automática para tus archivos importantes.",
        "Busca las actualizaciones pendientes del sistema y de tus aplicaciones.",
        "Activa la verificación en dos pasos en tus cuentas importantes.",
        "Revisa que las opciones de recuperación de tus cuentas estén actualizadas."
    )

    val indicesConRiesgo = preguntas.indices.filter {
        respuestas[it] == false
    }
    val nivelRiesgo = calcularNivelRiesgo(respuestas)
    val colorRiesgo = when(nivelRiesgo){
        "Nulo" -> MaterialTheme.colorScheme.secondary
        "Bajo"-> MaterialTheme.colorScheme.primary
        "Medio"-> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Resultado",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nivel de Riesgo: $nivelRiesgo",
                    color = colorRiesgo,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        if (indicesConRiesgo.isEmpty()) {
            Text("¡Bien! Marcaste que ya realizas todas estas acciones preventivas.")
        } else {
            Text("Estas son las áreas que puedes revisar:")
            Spacer(modifier = Modifier.height(12.dp))

            indicesConRiesgo.forEach { indice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = preguntas[indice],
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Recomendación: ${recomendaciones[indice]}"
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = accionesCompletadas[indice],
                                enabled = !reiniciandoProgreso,
                                onCheckedChange = { marcada ->
                                    alCambiarAccion(indice, marcada)
                                }
                            )

                            Text(
                                text = if (accionesCompletadas[indice]) {
                                    "Acción completada"
                                } else {
                                    "Marcar como completada"
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { confirmarReinicio = true },
            enabled = !reiniciandoProgreso
        ) {
            Text(if (reiniciandoProgreso) "Reiniciando…" else "Reiniciar progreso")
        }
        if (errorReinicio != null) {
            Text(errorReinicio, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = alVolver) {
            Text("Volver al inicio")
        }
    }
}
}
