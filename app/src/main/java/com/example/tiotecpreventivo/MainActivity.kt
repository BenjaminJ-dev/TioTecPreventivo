package com.example.tiotecpreventivo

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotecpreventivo.data.PreguntaEvaluacion
import com.example.tiotecpreventivo.ui.screens.AlertasAnciScreen
import com.example.tiotecpreventivo.ui.screens.EvaluacionTioTec
import com.example.tiotecpreventivo.ui.screens.InicioTioTec
import com.example.tiotecpreventivo.ui.screens.ResultadoTioTec
import com.example.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import com.example.tiotecpreventivo.ui.viewmodel.AlertasAnciViewModel
import com.example.tiotecpreventivo.ui.viewmodel.EvaluacionViewModel
import com.example.tiotecpreventivo.ui.viewmodel.TioTecViewModel
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    private var abrirResultados = mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        abrirResultados.value = intent.action == NotificacionHelper.ABRIR_RESULTADOS
    }

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
        RecordatorioPreventivoWorker.cancelarPeriodicoAnterior(this)
        abrirResultados.value = savedInstanceState == null &&
            intent.action == NotificacionHelper.ABRIR_RESULTADOS

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
                        modifier = Modifier.padding(innerPadding),
                        abrirResultados = abrirResultados.value,
                        alRecibirApertura = { abrirResultados.value = false }
                    )
                }
            }
        }
    }
}

@Composable
fun AppTioTec(
    modifier: Modifier = Modifier,
    abrirResultados: Boolean = false,
    alRecibirApertura: () -> Unit = {}
) {
    val pantalla = remember { mutableStateOf("inicio") }
    val modelo: TioTecViewModel = viewModel()
    val evaluacion: EvaluacionViewModel = viewModel()
    val resultado = remember { mutableStateOf<List<PreguntaEvaluacion>>(emptyList()) }
    val nombrePerfil = rememberSaveable { mutableStateOf("") }
    val perfilConfirmado = rememberSaveable { mutableStateOf(false) }
    val fotoDispositivo = remember { mutableStateOf<Bitmap?>(null) }

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        fotoDispositivo.value = foto
    }

    val contexto = LocalContext.current
    val abrirTrasCargar = remember { mutableStateOf(false) }
    val verResultado = {
        if (evaluacion.validarRespuestas()) {
            resultado.value = evaluacion.preguntas.sortedBy { it.id }
            pantalla.value = "resultado"
        }
    }
    LaunchedEffect(abrirResultados) {
        if (abrirResultados) {
            alRecibirApertura()
            abrirTrasCargar.value = true
            pantalla.value = "cargarResultado"
            evaluacion.cargar()
        }
    }
    LaunchedEffect(abrirTrasCargar.value, evaluacion.sincronizando, evaluacion.errorRed) {
        if (abrirTrasCargar.value && !evaluacion.sincronizando && evaluacion.errorRed == null) {
            abrirTrasCargar.value = false
            verResultado()
            if (pantalla.value == "cargarResultado") pantalla.value = "evaluacion"
        }
    }

    val recomendaciones = resultado.value.filter { it.respuesta == false }.map { it.id - 1 }
    val quedanCasillas = recomendaciones.any { !modelo.accionesCompletadas[it] }
    val salirDeResultado = {
        if (recomendaciones.any { !modelo.accionesCompletadas[it] }) {
            RecordatorioPreventivoWorker.programar(contexto, recomendaciones.toIntArray())
        } else {
            RecordatorioPreventivoWorker.cancelar(contexto)
        }
        pantalla.value = "inicio"
    }
    BackHandler(enabled = pantalla.value == "resultado", onBack = salirDeResultado)
    LaunchedEffect(pantalla.value, quedanCasillas) {
        if (pantalla.value == "resultado" && !quedanCasillas) {
            RecordatorioPreventivoWorker.cancelar(contexto)
        }
    }
    val explicarRecordatorios = rememberSaveable { mutableStateOf(false) }
    val explicacionMostrada = rememberSaveable { mutableStateOf(false) }
    val permisoNotificaciones = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    if (explicarRecordatorios.value) {
        AlertDialog(
            onDismissRequest = { explicarRecordatorios.value = false },
            title = { Text("Recordatorios preventivos") },
            text = { Text("Permite las notificaciones para que TíoTec pueda avisarte si sales de Resultados y todavía tienes recomendaciones sin marcar.") },
            confirmButton = {
                TextButton(onClick = {
                    explicarRecordatorios.value = false
                    permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = { explicarRecordatorios.value = false }) { Text("Ahora no") }
            }
        )
    }

    AnimatedContent(
        targetState = pantalla.value,
        label = "Transición entre pantallas"
    ) { pantallaActual ->
        when (pantallaActual) {
            "inicio" -> InicioTioTec(
                modifier = modifier,
                alComenzar = {
                    evaluacion.cargar()
                    pantalla.value = "evaluacion"
                },
                alVerAlertas = { pantalla.value = "alertas" },
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

            "cargarResultado" -> Column(modifier.padding(24.dp)) {
                Text("Resultado")
                Text(evaluacion.errorRed ?: "Cargando resultado…")
                if (evaluacion.errorRed != null) {
                    TextButton(onClick = evaluacion::cargar) { Text("Reintentar") }
                }
                TextButton(onClick = {
                    abrirTrasCargar.value = false
                    pantalla.value = "inicio"
                }) { Text("Volver al inicio") }
            }

            "evaluacion" -> EvaluacionTioTec(
                modifier = modifier,
                preguntas = evaluacion.preguntas,
                mostrarError = evaluacion.mostrarError,
                sincronizando = evaluacion.sincronizando,
                errorRed = evaluacion.errorRed,
                alReintentar = evaluacion::cargar,
                alCambiarRespuesta = evaluacion::cambiarRespuesta,
                alVolver = {
                    abrirTrasCargar.value = false
                    pantalla.value = "inicio"
                },
                alVerResultado = {
                    verResultado()
                    if (pantalla.value == "resultado" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !explicacionMostrada.value && ContextCompat.checkSelfPermission(
                            contexto, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        explicacionMostrada.value = true
                        explicarRecordatorios.value = true
                    }
                }
            )

            "resultado" -> ResultadoTioTec(
                modifier = modifier,
                preguntas = resultado.value.map { it.texto },
                respuestas = resultado.value.map { it.respuesta },
                accionesCompletadas = modelo.accionesCompletadas,
                reiniciandoProgreso = modelo.reiniciandoProgreso,
                errorReinicio = modelo.errorReinicio,
                alReiniciarProgreso = modelo::reiniciarProgreso,
                alCambiarAccion = { indice, marcada ->
                    modelo.cambiarAccion(indice, marcada)
                },
                alVolver = salirDeResultado
            )
        }
    }
}
