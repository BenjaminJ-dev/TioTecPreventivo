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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun InicioTioTec(
    modifier: Modifier = Modifier,
    alComenzar: () -> Unit,
    alProbarNotificacion: () -> Unit,
    fotoDispositivo: Bitmap?,
    alTomarFoto: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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

                Button(onClick = alTomarFoto) {
                    Text("Tomar foto del dispositivo")
                }

                fotoDispositivo?.let { foto ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        bitmap = foto.asImageBitmap(),
                        contentDescription = "Foto del dispositivo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }
    }
}