package com.azure.tcpdump

// MainScreen.kt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PacketViewModel) {
    val packets by viewModel.packets.collectAsState()
    val selectedPacket by viewModel.selectedPacket.collectAsState()
    val isCapturing by remember { viewModel.isCapturing }

    //val displayFormat by viewModel.displayFormat.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据包", fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // 控制按钮
                    Button(onClick = { viewModel.toggleCapture() }) {
                        Text(
                            text = if (isCapturing) "STOP" else "START",
                        )
                        if (isCapturing) viewModel.startVpnService() else viewModel.stopVpnService()
                    }
                    Button(onClick = { viewModel.clearPackets() }) {
                        Text("清空")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 左侧列表
            PacketList(
                packets = packets,
                selectedPacket = selectedPacket,
                onPacketClick = { viewModel.selectPacket(it) },
                modifier = Modifier.weight(1f)
            )

            // 右侧详情
            /*if (selectedPacket != null) {
                PacketDetail(
                    packet = selectedPacket!!,
                    displayFormat = displayFormat,
                    onFormatChange = { viewModel.setDisplayFormat(it) },
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                )
            } else {
                // 无选中时的提示
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "选择一个数据包查看详情",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }*/
        }
    }
}

@Composable
fun PacketList(
    packets: List<Packet>,
    selectedPacket: Packet?,
    onPacketClick: (Packet) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 过滤器

        /*OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            label = { Text("过滤协议或地址") },
            //leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )*/

        // 数据包列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (packets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "没有数据包",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(packets) { packet ->
                    PacketListItem(
                        packet = packet,
                        isSelected = selectedPacket?.id == packet.id,
                        onClick = { onPacketClick(packet) }
                    )
                }
            }
        }
    }
}

@Composable
fun PacketListItem(
    packet: Packet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    //val isExpand by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 协议行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                //ProtocolIcon(protocol = packet.protocol)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = packet.protocol.name,
                    fontWeight = FontWeight.Bold,
                    color = getProtocolColor(packet.protocol),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${packet.size} bytes",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 地址行
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${packet.sourceIp}:${packet.sourcePort}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "->",
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = "${packet.destinationIp}:${packet.destinationPort}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }

            // 摘要
            if (packet.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = packet.summary,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

/*@Composable
fun ProtocolIcon(protocol: Protocol) {
    val icon = when (protocol) {
        Protocol.HTTP -> Icons.Default.Http
        Protocol.HTTPS -> Icons.Default.Lock
        Protocol.TCP -> Icons.Default.Lan
        Protocol.UDP -> Icons.Default.Lan
        Protocol.DNS -> Icons.Default.Dns
        Protocol.ICMP -> Icons.Default.NetworkPing
        Protocol.ARP -> Icons.Default.DeviceHub
        Protocol.OTHER -> Icons.Default.SettingsEthernet
    }

    Icon(
        imageVector = icon,
        contentDescription = protocol.name,
        modifier = Modifier.size(20.dp),
        tint = getProtocolColor(protocol)
    )
}*/

@Composable
fun getProtocolColor(protocol: Protocol) = when (protocol) {
    Protocol.HTTP -> MaterialTheme.colorScheme.primary
    Protocol.HTTPS -> MaterialTheme.colorScheme.tertiary
    Protocol.TCP -> MaterialTheme.colorScheme.secondary
    Protocol.UDP -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
    Protocol.DNS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    Protocol.ICMP -> MaterialTheme.colorScheme.error
    Protocol.ARP -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    Protocol.OTHER -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
}