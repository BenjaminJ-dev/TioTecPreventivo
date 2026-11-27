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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import com.example.tiotecpreventivo.data.PreguntaEvaluacion

@Composable
fun EvaluacionTioTec(
    modifier: Modifier = Modifier,
    preguntas: List<PreguntaEvaluacion>,
    mostrarError: Boolean,
    sincronizando: Boolean,
    errorRed: String?,
    alReintentar: () -> Unit,
    alCambiarRespuesta: (Int, Boolean) -> Unit,
    alVolver: () -> Unit,
    alVerResultado: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Evaluación preventiva",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))
        if (sincronizando) {
            Text(if (preguntas.isEmpty()) "Cargando preguntas…" else "Sincronizando evaluación…")
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (errorRed != null) {
            Text(errorRed, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = alReintentar, enabled = !sincronizando) {
                Text("Reintentar")
            }
        }
        if (preguntas.isNotEmpty()) Text("Selecciona una respuesta para cada pregunta.")
        Spacer(modifier = Modifier.height(16.dp))

        preguntas.forEachIndexed { indice, pregunta ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${indice + 1}. ${pregunta.texto}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        Row {
                            RadioButton(
                                enabled = !sincronizando && errorRed == null,
                                selected = pregunta.respuesta == true,
                                onClick = {
                                    alCambiarRespuesta(pregunta.id, true)
                                }
                            )
                            Text(
                                text = "Sí",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Row {
                            RadioButton(
                                enabled = !sincronizando && errorRed == null,
                                selected = pregunta.respuesta == false,
                                onClick = {
                                    alCambiarRespuesta(pregunta.id, false)
                                }
                            )
                            Text(
                                text = "No",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }

                    if (mostrarError && pregunta.respuesta == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Respuesta pendiente",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Selecciona Sí o No para continuar.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                    }
                }
            }
        }

        if (mostrarError && preguntas.isNotEmpty()) {
            Text(
                text = "Responde todas las preguntas para continuar.",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = alVerResultado,
            enabled = preguntas.isNotEmpty() && !sincronizando && errorRed == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver resultado")
        }

        TextButton(onClick = alVolver) {
            Text("Volver al inicio")
        }
    }
}
