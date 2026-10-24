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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotecpreventivo.ui.screens.EvaluacionTioTec
import com.example.tiotecpreventivo.ui.screens.InicioTioTec
import com.example.tiotecpreventivo.ui.screens.ResultadoTioTec
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
