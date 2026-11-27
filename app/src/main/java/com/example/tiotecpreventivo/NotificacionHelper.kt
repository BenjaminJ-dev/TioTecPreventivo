package com.example.tiotecpreventivo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificacionHelper {

    private const val ID_CANAL = "recordatorios_preventivos"
    const val ABRIR_RESULTADOS = "com.example.tiotecpreventivo.ABRIR_RESULTADOS"

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

    fun puedeMostrar(contexto: Context): Boolean =
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(contexto).areNotificationsEnabled()

    @SuppressLint("MissingPermission")
    fun mostrar(contexto: Context) {
        if (!puedeMostrar(contexto)) return
        crearCanal(contexto)
        val abrirTioTec = PendingIntent.getActivity(
            contexto,
            0,
            Intent(contexto, MainActivity::class.java)
                .setAction(ABRIR_RESULTADOS)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(contexto, ID_CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("TíoTec Preventivo")
            .setContentText(
                "Tienes recomendaciones preventivas sin marcar."
            )
            .setContentIntent(abrirTioTec)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val administrador =
            contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        administrador.notify(1, notificacion)
    }
}
