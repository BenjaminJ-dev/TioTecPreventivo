package com.example.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.example.tiotecpreventivo.data.AlertaAnci
import com.example.tiotecpreventivo.ui.viewmodel.EstadoAlertasAnci
import java.net.URI

@Composable
fun AlertasAnciScreen(
    estado: EstadoAlertasAnci,
    alReintentar: () -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = alVolver) {
            Text("Volver al inicio")
        }

        when (estado) {
            EstadoAlertasAnci.Cargando -> {
                TituloEstafasAnci()
                CircularProgressIndicator()
                Text("Cargando alertas de ANCI…")
            }

            is EstadoAlertasAnci.Error -> {
                TituloEstafasAnci()
                Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                Button(onClick = alReintentar) {
                    Text("Reintentar")
                }
            }

            is EstadoAlertasAnci.Recibidas -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TituloEstafasAnci()
                        Text("Si un mensaje te pide datos o un pago, verifica la solicitud por un canal oficial antes de actuar.")
                    }
                    if (estado.alertas.isEmpty()) {
                        item { Text("No hay campañas fraudulentas en las alertas consultadas.") }
                    }
                    items(estado.alertas) { alerta ->
                        TarjetaAlertaAnci(alerta)
                    }
                }
            }
        }
    }
}

@Composable
private fun TituloEstafasAnci() {
    Text(
        "Estafas que circulan en Chile y cómo protegerse",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
private fun TarjetaAlertaAnci(alerta: AlertaAnci) {
    val navegador = LocalUriHandler.current
    val resumen = alerta.descripcionEspecifica?.takeIf { it.isNotBlank() }
        ?: alerta.descripcionGeneral
    val entidades = alerta.entidadesAfectadas.orEmpty()
        .filter { it.isNotBlank() }
        .joinToString(", ")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = alerta.titulo?.takeIf { it.isNotBlank() } ?: "Alerta sin título",
                style = MaterialTheme.typography.titleMedium
            )
            CampoAlerta("Entidades afectadas según ANCI", entidades)
            CampoAlerta("Fecha", alerta.fecha?.substringBefore('T'))
            CampoAlerta("Resumen", resumen)
            CampoAlerta("Mitigación", alerta.mitigacion)
            alerta.enlacesRelacionados.orEmpty().forEach { enlace ->
                val url = enlace.url
                if (url != null && esEnlaceOficial(url)) {
                    TextButton(onClick = { navegador.openUri(url) }) {
                        Text(enlace.nombre?.takeIf { it.isNotBlank() } ?: "Ver enlace oficial")
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoAlerta(etiqueta: String, valor: String?) {
    if (!valor.isNullOrBlank()) {
        val texto = HtmlCompat.fromHtml(valor, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
        if (texto.isNotEmpty()) {
            Text("$etiqueta: $texto")
        }
    }
}

private fun esEnlaceOficial(url: String): Boolean {
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    return uri.scheme.equals("https", ignoreCase = true) && uri.userInfo == null &&
        (uri.port == -1 || uri.port == 443) &&
        uri.host?.lowercase() in setOf("anci.gob.cl", "www.anci.gob.cl", "csirt.gob.cl", "www.csirt.gob.cl")
}
