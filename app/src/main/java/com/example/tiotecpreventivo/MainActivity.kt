package com.example.tiotecpreventivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import androidx.compose.material3.Checkbox
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.tiotecpreventivo.data.DatabaseProvider
import com.example.tiotecpreventivo.data.AccionPreventiva
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
    val respuestas = remember {
        mutableStateListOf<Boolean?>(null, null, null, null)
    }
    val mostrarError = remember { mutableStateOf(false) }

    val accionesCompletadas = remember {
        mutableStateListOf(false, false, false, false)

    }

    val contexto = LocalContext.current
    val dao = remember {
        DatabaseProvider.obtenerBaseDeDatos(contexto)
            .accionPreventivaDao()
    }
    val alcance = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val accionesGuardadas = dao.obtenerTodas()

        accionesGuardadas.forEach { accion ->
            if (accion.id in accionesCompletadas.indices) {
                accionesCompletadas[accion.id] = accion.completada
            }
        }
    }

    val preguntas = listOf(
        "¿Realizas copias de seguridad de tus archivos importantes?",
        "¿Mantienes actualizado tu teléfono y sus aplicaciones?",
        "¿Usas una contraseña o método adicional de verificación en tus cuentas?",
        "¿Tienes actualizado un correo o teléfono para recuperar tus cuentas?"
    )

    when (pantalla.value) {
        "inicio" -> InicioTioTec(
            modifier = modifier,
            alComenzar = { pantalla.value = "evaluacion" }
        )

        "evaluacion" -> EvaluacionTioTec(
            modifier = modifier,
            preguntas = preguntas,
            respuestas = respuestas,
            mostrarError = mostrarError.value,
            alVolver = { pantalla.value = "inicio" },
            alVerResultado = {
                if (respuestas.all { it != null }) {
                    mostrarError.value = false
                    pantalla.value = "resultado"
                } else {
                    mostrarError.value = true
                }
            }
        )

        "resultado" -> ResultadoTioTec(
            modifier = modifier,
            preguntas = preguntas,
            respuestas = respuestas,
            accionesCompletadas = accionesCompletadas,
            alCambiarAccion = { indice, marcada ->
                accionesCompletadas[indice] = marcada

                alcance.launch {
                    dao.guardar(
                        AccionPreventiva(
                            id = indice,
                            completada = marcada
                        )
                    )
                }
            },
            alVolver = { pantalla.value = "inicio" }
        )
    }
}

@Composable
fun InicioTioTec(
    modifier: Modifier = Modifier,
    alComenzar: () -> Unit
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
            }
        }
    }
}

@Composable
fun EvaluacionTioTec(
    modifier: Modifier = Modifier,
    preguntas: List<String>,
    respuestas: MutableList<Boolean?>,
    mostrarError: Boolean,
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
                                onClick = { respuestas[indice] = true }
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
                                onClick = { respuestas[indice] = false }
                            )
                            Text(
                                text = "No",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
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
    accionesCompletadas: MutableList<Boolean>,
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