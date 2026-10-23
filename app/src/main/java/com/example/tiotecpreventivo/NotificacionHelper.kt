package com.example.tiotecpreventivo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificacionHelper {

    private const val ID_CANAL = "recordatorios_preventivos"

    fun crearCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                ID_CANAL,
                "Recordatorios preventivos",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            canal.description =
                "Avisos para revisar las acciones de seguridad digital"

            val administrador =
                contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            administrador.createNotificationChannel(canal)
        }
    }

    fun mostrar(contexto: Context) {
        val notificacion = NotificationCompat.Builder(contexto, ID_CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("TíoTec Preventivo")
            .setContentText(
                "Es un buen momento para revisar tus acciones de seguridad digital."
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val administrador =
            contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        administrador.notify(1, notificacion)
    }
}