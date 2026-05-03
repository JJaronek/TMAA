package com.example.tmap.core.notifications // Zkontroluj, že package sedí podle toho, kam soubor dáš

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TmapNotificationManager(private val context: Context) {

    private val channelId = "tmap_leaderboard_channel"
    private val notificationId = 1

    init {
        createNotificationChannel()
    }

    // Android vyžaduje vytvoření "kanálu" pro notifikace
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Světový žebříček"
            val descriptionText = "Notifikace o změnách ve světovém žebříčku TMAP"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showLeaderboardUpdateNotification(message: String) {
        // Kontrola, jestli nám uživatel vůbec povolil notifikace
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Pokud nemáme povolení, prostě nic neukážeme (řešení povolení uděláme v UI)
                return
            }
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Výchozí Android ikona
            .setContentTitle("Změna v žebříčku! 🎯")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}