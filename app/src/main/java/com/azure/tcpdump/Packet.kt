package com.azure.tcpdump

import java.util.UUID

data class Packet(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val protocol: Protocol,
    val sourceIp: String,
    val sourcePort: Int,
    val destinationIp: String,
    val destinationPort: Int,
    val size: Int,
    val summary: String = "",
    val rawData: ByteArray = byteArrayOf(),
    val isSelected: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Packet

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

}

enum class Protocol {
    TCP, UDP, HTTP, HTTPS, DNS, ICMP, ARP, OTHER
}
