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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultadoTioTec(
    modifier: Modifier = Modifier,
    preguntas: List<String>,
    respuestas: List<Boolean?>,
    accionesCompletadas: List<Boolean>,
    alCambiarAccion: (Int, Boolean) -> Unit,
    alVolver: () -> Unit
) {
    val recomendaciones = listOf(
        "Activa una copia de seguridad automática para tus archivos importantes.",
        "Busca las actualizaciones pendientes del sistema y de tus aplicaciones.",
        "Activa la verificación en dos pasos en tus cuentas importantes.",
        "Revisa que las opciones de recuperación de tus cuentas estén actualizadas."
    )

    val indicesConRiesgo = preguntas.indices.filter {
        respuestas[it] == false
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

        Button(onClick = alVolver) {
            Text("Volver al inicio")
        }
    }
}