package com.azure.tcpdump

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PacketViewModel(application: Application) : AndroidViewModel(application) {
    private val _packets = MutableStateFlow<List<Packet>>(emptyList())
    val packets = _packets.asStateFlow()

    private val _selectedPacket = MutableStateFlow<Packet?>(null)
    val selectedPacket = _selectedPacket.asStateFlow()

    var isCapturing = mutableStateOf(false)
    var isVpnRunning = false
    val filterText = mutableStateOf("")
    val mApplication = getApplication<Application>()
    
    fun selectPacket(packet: Packet) {
        viewModelScope.launch {
            _selectedPacket.value = packet
        }
    }

    fun clearSelection() {
        viewModelScope.launch {
            _selectedPacket.value = null
        }
    }

    fun toggleCapture() {
        isCapturing.value = !isCapturing.value
    }

    fun clearPackets() {
        viewModelScope.launch {
            _packets.value = emptyList()
            _selectedPacket.value = null
        }
    }

    fun addPacket(packet: Packet) {
        viewModelScope.launch {
            _packets.value += packet
        }
    }

    fun getFilteredPackets(): List<Packet> {
        return if (filterText.value.isBlank()) {
            _packets.value
        } else {
            _packets.value.filter { packet ->
                packet.sourceIp.contains(filterText.value, true) ||
                        packet.destinationIp.contains(filterText.value, true) ||
                        packet.protocol.name.contains(filterText.value, true) ||
                        packet.summary.contains(filterText.value, true)
            }
        }
    }

    fun startVpnService() {
        if (isVpnRunning)
            return

        val startVpnIntent = Intent(mApplication.applicationContext, TcpDumpVpnService::class.java).apply {
            action = TcpDumpVpnService.ACTION_START
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mApplication.applicationContext.startForegroundService(startVpnIntent)
        } else {
            mApplication.applicationContext.startService(startVpnIntent)
        }
        isVpnRunning = true

    }

    fun stopVpnService() {
        if (!isVpnRunning)
            return

        val stopVpnIntent = Intent(mApplication.applicationContext, TcpDumpVpnService::class.java).apply {
            action = TcpDumpVpnService.ACTION_STOP
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mApplication.applicationContext.startForegroundService(stopVpnIntent)
        } else {
            mApplication.applicationContext.startService(stopVpnIntent)
        }
        isVpnRunning = false
    }


    fun loadSampleData() {
        viewModelScope.launch {
            val samplePackets = listOf(
                Packet(
                    protocol = Protocol.HTTP,
                    sourceIp = "192.168.1.100",
                    sourcePort = 54321,
                    destinationIp = "93.184.216.34",
                    destinationPort = 80,
                    size = 512,
                    summary = "GET / HTTP/1.1"
                ),
                Packet(
                    protocol = Protocol.HTTPS,
                    sourceIp = "192.168.1.100",
                    sourcePort = 54322,
                    destinationIp = "142.250.189.78",
                    destinationPort = 443,
                    size = 1024,
                    summary = "Client Hello"
                ),
                Packet(
                    protocol = Protocol.DNS,
                    sourceIp = "192.168.1.100",
                    sourcePort = 5353,
                    destinationIp = "8.8.8.8",
                    destinationPort = 53,
                    size = 84,
                    summary = "Query: example.com"
                )
            )
            _packets.value = samplePackets
        }
    }
}