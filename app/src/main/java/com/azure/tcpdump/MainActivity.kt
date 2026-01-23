package com.azure.tcpdump

import android.Manifest
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.azure.tcpdump.ui.theme.TcpDumpTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PacketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vpnPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != RESULT_OK) {
                Toast.makeText(this, "未授予创建VPN连接权限", Toast.LENGTH_SHORT).show()
                throw Exception("权限不足")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        setContent {
            TcpDumpTheme {
                viewModel.loadSampleData()
                MainScreen(viewModel = viewModel)

                VpnService.prepare(this)?.let { vpnPermissionLauncher.launch(it) }
                vpnPermissionLauncher.unregister()

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