package com.example.tiotecpreventivo.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextButton

@Composable
fun InicioTioTec(
    modifier: Modifier = Modifier,
    alComenzar: () -> Unit,
    alVerAlertas: () -> Unit,
    alProbarNotificacion: () -> Unit,
    nombrePerfil: String,
    alCambiarNombre: (String) -> Unit,
    fotoPerfil: Bitmap?,
    alTomarFoto: () -> Unit,
    perfilConfirmado: Boolean,
    alConfirmarNombre: () -> Unit,
    alEditarNombre: () -> Unit,


) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TíoTec",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Tu guía de prevención digital",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = alVerAlertas) {
            Text("Ver alertas de ciberseguridad")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Revisa tus hábitos de seguridad",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Responde una evaluación breve y descubre qué acciones preventivas puedes realizar."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = alComenzar) {
                    Text("Comenzar evaluación")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = alProbarNotificacion) {
                    Text("Probar recordatorio")
                }
                Text(
                    text = "Crea tu perfil de prueba",
                    style = MaterialTheme.typography.titleMedium
                )
                if (!perfilConfirmado) {
                    OutlinedTextField(
                        value = nombrePerfil,
                        onValueChange = alCambiarNombre,
                        label = { Text("Nombre de usuario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = alConfirmarNombre,
                        enabled = nombrePerfil.isNotBlank()
                    ) {
                        Text("Guardar nombre")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (fotoPerfil != null) {
                            Image(
                                bitmap = fotoPerfil.asImageBitmap(),
                                contentDescription = "Foto de perfil de $nombrePerfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Foto")
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nombrePerfil,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(onClick = alTomarFoto) {
                                Text(
                                    if (fotoPerfil == null) {
                                        "Agregar foto de perfil"
                                    } else {
                                        "Cambiar foto de perfil"
                                    }
                                )
                            }

                            TextButton(onClick = alEditarNombre) {
                                Text("Cambiar nombre")
                            }
                        }
                    }
                }
            }
        }
    }
}


