package com.example.tiotecpreventivo

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotecpreventivo.ui.screens.AlertasAnciScreen
import com.example.tiotecpreventivo.ui.screens.EvaluacionTioTec
import com.example.tiotecpreventivo.ui.screens.InicioTioTec
import com.example.tiotecpreventivo.ui.screens.ResultadoTioTec
import com.example.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import com.example.tiotecpreventivo.ui.viewmodel.AlertasAnciViewModel
import com.example.tiotecpreventivo.ui.viewmodel.TioTecViewModel
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    private val permisoRedLocal = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            ViewModelProvider(this)[TioTecViewModel::class.java].sincronizarChecklist()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificacionHelper.crearCanal(this)

        if (savedInstanceState == null &&
            Build.VERSION.SDK_INT >= 37 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCAL_NETWORK) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            permisoRedLocal.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }

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
    val nombrePerfil = rememberSaveable { mutableStateOf("") }
    val perfilConfirmado = rememberSaveable { mutableStateOf(false) }
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
                alVerAlertas = { pantalla.value = "alertas" },
                alProbarNotificacion = probarNotificacion,
                nombrePerfil = nombrePerfil.value,
                perfilConfirmado = perfilConfirmado.value,
                alConfirmarNombre = { perfilConfirmado.value = true },
                alEditarNombre = { perfilConfirmado.value = false },
                alCambiarNombre = { nuevoNombre ->
                    nombrePerfil.value = nuevoNombre
                },
                fotoPerfil = fotoDispositivo.value,
                alTomarFoto = { lanzadorCamara.launch(null) }
            )

            "alertas" -> {
                val modeloAlertas: AlertasAnciViewModel = viewModel()
                AlertasAnciScreen(
                    estado = modeloAlertas.estado,
                    alReintentar = modeloAlertas::reintentar,
                    alVolver = { pantalla.value = "inicio" },
                    modifier = modifier
                )
            }

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
                reiniciandoProgreso = modelo.reiniciandoProgreso,
                errorReinicio = modelo.errorReinicio,
                alReiniciarProgreso = modelo::reiniciarProgreso,
                alCambiarAccion = { indice, marcada ->
                    modelo.cambiarAccion(indice, marcada)
                },
                alVolver = { pantalla.value = "inicio" }
            )
        }
    }
}
