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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PacketViewModel) {
    var packets by viewModel.packets
    val isCapturing by viewModel.isCapturing

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.clearPackets() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_cleaning_services_24),
                            contentDescription = stringResource(R.string.clean_packets)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleCapture() }
            ) {
                if (isCapturing) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_pause_24),
                        contentDescription = stringResource(R.string.stop_VPN)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.baseline_play_arrow_24),
                        contentDescription = stringResource(R.string.start_VPN)
                    )
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PacketList(
                packets = packets,
                onPacketClick = {  },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PacketList(
    packets: List<Packet>,
    onPacketClick: (Packet) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                    textAlign = TextAlign.End
                )
            }

            if (packet.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = packet.summary,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

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