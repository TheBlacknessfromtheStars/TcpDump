package com.azure.tcpdump

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf

var isMyVpnServiceRunning = mutableStateOf(false)

class MyVpnService : VpnService() {
    private val mConfigureIntent: PendingIntent by lazy {
        var activityFlag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activityFlag += PendingIntent.FLAG_MUTABLE
        }
        PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), activityFlag)
    }

    private lateinit var vpnInterface: ParcelFileDescriptor

    override fun onCreate() {
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (intent?.action == ACTION_DISCONNECT) {
            disconnect()
            START_NOT_STICKY
        } else {
            connect()
            Log.i("MyVpnService", "onStartCommand: connect")
            START_STICKY
        }
    }

    private fun connect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updateForegroundNotification()
        }
        vpnInterface = createVpnInterface()
        val fileDescriptor = vpnInterface.fileDescriptor
        isMyVpnServiceRunning.value = true
    }

    private fun disconnect() {
        vpnInterface.close()
        isMyVpnServiceRunning.value = false
        System.gc()
    }

    private fun createVpnInterface(): ParcelFileDescriptor {
        return Builder()
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("114.114.114.114")
            .setSession("VPN-Demo")
            .setBlocking(true)
            .setConfigureIntent(mConfigureIntent)
            .also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.setMetered(false)
                }
            }
            .establish() ?: throw IllegalStateException("无法初始化vpnInterface")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateForegroundNotification() {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    companion object {
        /**
         * 通知频道Id
         */
        const val NOTIFICATION_CHANNEL_ID = "MyVpnService"

        /**
         * 动作：连接
         */
        const val ACTION_CONNECT = "com.azure.tcpdump.MyVpnService.CONNECT"

        /**
         * 动作：断开连接
         */
        const val ACTION_DISCONNECT = "com.azure.tcpdump.MyVpnService.DISCONNECT"
    }
}