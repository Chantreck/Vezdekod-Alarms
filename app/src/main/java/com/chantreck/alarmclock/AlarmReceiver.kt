package com.chantreck.alarmclock

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "123"
        private const val NOTIFICATION_CHANNEL_NAME = "Будильники"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.extras?.getInt("ID") ?: -1

        if (context == null) return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager: NotificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val builder2 = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setContentTitle("Будильник")

        val manager = NotificationManagerCompat.from(context)
        manager.notify(id, builder2.build())

        try {
            MainActivity.getInstance()?.onAlarmStart()
        } catch (e: Exception) {
            println(e.toString())
        }
    }
}