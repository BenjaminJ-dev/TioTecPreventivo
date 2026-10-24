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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EvaluacionTioTec(
    modifier: Modifier = Modifier,
    preguntas: List<String>,
    respuestas: List<Boolean?>,
    mostrarError: Boolean,
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
        Text("Selecciona una respuesta para cada pregunta.")
        Spacer(modifier = Modifier.height(16.dp))

        preguntas.forEachIndexed { indice, pregunta ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${indice + 1}. $pregunta",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        Row {
                            RadioButton(
                                selected = respuestas[indice] == true,
                                onClick = {
                                    alCambiarRespuesta(indice, true)
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
                                selected = respuestas[indice] == false,
                                onClick = {
                                    alCambiarRespuesta(indice, false)
                                }
                            )
                            Text(
                                text = "No",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }

                    if (mostrarError && respuestas[indice] == null) {
                        Text(
                            text = "⚠️ Selecciona Sí o No para continuar.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        if (mostrarError) {
            Text(
                text = "Responde todas las preguntas para continuar.",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = alVerResultado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver resultado")
        }

        TextButton(onClick = alVolver) {
            Text("Volver al inicio")
        }
    }
}