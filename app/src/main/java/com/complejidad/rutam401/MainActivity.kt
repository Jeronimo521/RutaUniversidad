package com.complejidad.rutam401

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.complejidad.rutam401.ui.AppRoot
import com.complejidad.rutam401.ui.NfcStatus

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null
    private var nfcStatus by mutableStateOf(NfcStatus.UNAVAILABLE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContent {
            AppRoot(
                viewModel = viewModel,
                nfcStatus = nfcStatus,
                onOpenNfcSettings = { startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter = nfcAdapter
        nfcStatus = when {
            adapter == null -> NfcStatus.UNAVAILABLE
            adapter.isEnabled -> NfcStatus.READY
            else -> NfcStatus.DISABLED
        }
        if (nfcStatus == NfcStatus.READY) {
            // Reader mode sends scans straight to this screen while the app is open.
            adapter?.enableReaderMode(
                this,
                { tag ->
                    val uid = tag.id.joinToString("") { byte -> "%02X".format(byte) }
                    runOnUiThread { viewModel.onTagRead(uid) }
                },
                NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null,
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcStatus == NfcStatus.READY) {
            nfcAdapter?.disableReaderMode(this)
        }
    }
}
