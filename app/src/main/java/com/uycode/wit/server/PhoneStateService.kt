package com.uycode.wit.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import com.uycode.wit.R


class PhoneStateService: Service() {

    private lateinit var phoneStateReceiver: PhoneStateReceiver

    override fun onCreate() {
        super.onCreate()
        phoneStateReceiver = PhoneStateReceiver()
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(phoneStateReceiver, filter)
    }

    //@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(phoneStateReceiver)
    }

    private fun createNotification(): Notification {
        // Create and return a notification for the foreground service
        // This is required for Android 8.0 (API level 26) and above

        val channelId = createNotificationChannel("my_wit_service_channel", "Phone State Service in Foreground")
        return Notification.Builder(this, channelId)
            .setContentTitle("Phone State Server")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.unknown_isp)
            .build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}