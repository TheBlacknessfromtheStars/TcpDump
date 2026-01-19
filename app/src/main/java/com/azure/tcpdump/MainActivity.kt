package com.azure.tcpdump

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.azure.tcpdump.ui.theme.TcpDumpTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PacketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TcpDumpTheme {
                viewModel.loadSampleData()
                MainScreen(viewModel = viewModel)
                /*Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Spacer(modifier = Modifier.padding(innerPadding))

                }*/
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        TcpDumpTheme {
            MainScreen(viewModel = viewModel)
        }
    }

}