package com.azure.tcpdump

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PacketViewModel(application: Application) : AndroidViewModel(application) {
    val packets = mutableStateOf<List<Packet>>(emptyList())
    var isCapturing = mutableStateOf(false)
    val mApplication = getApplication<Application>()

    fun toggleCapture() {
        isCapturing.value = !isCapturing.value
        if (isCapturing.value) {
            startVpnService()
        } else {
            stopVpnService()
        }
    }

    fun clearPackets() {
        viewModelScope.launch {
            packets.value = emptyList()
        }
    }

    fun addPacket(packet: Packet) {
        viewModelScope.launch {
            packets.value += packet
        }
    }

    fun startVpnService() {
        val startVpnIntent = Intent(mApplication.applicationContext, TcpDumpVpnService::class.java).apply {
            action = TcpDumpVpnService.ACTION_START
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mApplication.applicationContext.startForegroundService(startVpnIntent)
        } else {
            mApplication.applicationContext.startService(startVpnIntent)
        }

    }

    fun stopVpnService() {
        val stopVpnIntent = Intent(mApplication.applicationContext, TcpDumpVpnService::class.java).apply {
            action = TcpDumpVpnService.ACTION_STOP
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mApplication.applicationContext.startForegroundService(stopVpnIntent)
        } else {
            mApplication.applicationContext.startService(stopVpnIntent)
        }
    }
}