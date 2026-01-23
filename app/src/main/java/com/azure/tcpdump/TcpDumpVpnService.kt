package com.azure.tcpdump

 import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor

class TcpDumpVpnService : VpnService() {
    private val mConfigureIntent: PendingIntent by lazy {
        var activityFlag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activityFlag += PendingIntent.FLAG_MUTABLE
        }
        PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), activityFlag)
    }

    private lateinit var vpnInterface: ParcelFileDescriptor

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)

        vpnInterface.close()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                vpnInterface.close()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                setupVpn()
                updateForegroundNotification("running")

                return START_STICKY
            }
        }

        return START_NOT_STICKY
    }

    private fun setupVpn() {
        vpnInterface = Builder()
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .setSession("TcpDumpVPNService")
            .setConfigureIntent(mConfigureIntent)
            .also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.setMetered(false)
                }
            }
            .establish() ?: throw IllegalStateException("无法初始化vpnInterface")
    }

    private fun updateForegroundNotification(message: String) {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // 构建通知
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+ 需要通知渠道
            val channelName = "VPN服务"
            val importance = NotificationManager.IMPORTANCE_LOW

            // 创建或获取通知渠道
            var channel = mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (channel == null) {
                channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance).apply {
                    description = "显示VPN服务运行状态"
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(false)
                }
                mNotificationManager.createNotificationChannel(channel)
            }

            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            // Android 8.0 以下不需要渠道
            Notification.Builder(this)
        }.apply {
            setContentIntent(mConfigureIntent)
            setContentTitle("VPN Service")
            setContentText(message)
            setSmallIcon(R.drawable.ic_launcher_background)
            setOngoing(true)
            setWhen(System.currentTimeMillis())
            setShowWhen(true)
        }.build()

        // 启动前台服务
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14+ 需要指定前台服务类型
                startForeground(
                    1,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // Android 8.0+ 使用带渠道的方法
                startForeground(1, notification)
            }
            else -> {
                // Android 8.0 以下
                startForeground(1, notification)
            }
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "TcpDumpVpnService"
        const val TAG = NOTIFICATION_CHANNEL_ID
        const val ACTION_STOP = "com.azure.tcpdump.ACTION_STOP"
        const val ACTION_START = "com.azure.tcpdump.ACTION_START"
    }
}