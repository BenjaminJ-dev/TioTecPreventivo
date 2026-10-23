package com.example.tiotecpreventivo

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotecpreventivo.ui.screens.InicioTioTec
import com.example.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import com.example.tiotecpreventivo.ui.viewmodel.TioTecViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificacionHelper.crearCanal(this)

        setContent {
            TioTecPreventivoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppTioTec(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppTioTec(modifier: Modifier = Modifier) {
    val pantalla = remember { mutableStateOf("inicio") }
    val modelo: TioTecViewModel = viewModel()

    val fotoDispositivo = remember { mutableStateOf<Bitmap?>(null) }

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        fotoDispositivo.value = foto
    }

    val contexto = LocalContext.current

    val permisoNotificaciones = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            NotificacionHelper.mostrar(contexto)
        }
    }

    val probarNotificacion = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificacionHelper.mostrar(contexto)
        }
    }

    val preguntas = listOf(
        "¿Realizas copias de seguridad de tus archivos importantes?",
        "¿Mantienes actualizado tu teléfono y sus aplicaciones?",
        "¿Usas una contraseña o método adicional de verificación en tus cuentas?",
        "¿Tienes actualizado un correo o teléfono para recuperar tus cuentas?"
    )

    AnimatedContent(
        targetState = pantalla.value,
        label = "Transición entre pantallas"
    ) { pantallaActual ->
        when (pantallaActual) {
            "inicio" -> InicioTioTec(
                modifier = modifier,
                alComenzar = { pantalla.value = "evaluacion" },
                alProbarNotificacion = probarNotificacion,
                fotoDispositivo = fotoDispositivo.value,
                alTomarFoto = { lanzadorCamara.launch(null) }
            )
        "evaluacion" -> EvaluacionTioTec(
            modifier = modifier,
            preguntas = preguntas,
            respuestas = modelo.respuestas,
            mostrarError = modelo.mostrarError,
            alCambiarRespuesta = { indice, respuesta ->
                modelo.cambiarRespuesta(indice, respuesta)
            },
            alVolver = { pantalla.value = "inicio" },
            alVerResultado = {
                if (modelo.validarRespuestas()) {
                    pantalla.value = "resultado"
                }
            }
        )

        "resultado" -> ResultadoTioTec(
            modifier = modifier,
            preguntas = preguntas,
            respuestas = modelo.respuestas,
            accionesCompletadas = modelo.accionesCompletadas,
            alCambiarAccion = { indice, marcada ->
                modelo.cambiarAccion(indice, marcada)
            },
            alVolver = { pantalla.value = "inicio" }
        )
        }
    }
}
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
                                onClick = { alCambiarRespuesta(indice, true) }
                            )
                            Text(
                                text = "Sí",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.padding(horizontal = 12.dp))

                        Row {
                            RadioButton(
                                selected = respuestas[indice] == false,
                                onClick = { alCambiarRespuesta(indice, false) }
                            )
                            Text(
                                text = "No",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                    if (mostrarError && respuestas[indice] == null) {
                        Text(
                            text = "Selecciona Sí o No para continuar.",
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

                        Text(text = "Recomendación: ${recomendaciones[indice]}")

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