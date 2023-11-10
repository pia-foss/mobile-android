package com.kape.networkmanagement.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.compose.runtime.mutableStateOf

class NetworkScanner(private val context: Context) {

    var scanResults = mutableStateOf(emptyList<ScanResult>())

    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            scanResults.value = wifiManager.scanResults
        }
    }

    fun scanNetworks() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiReceiver, intentFilter)

        val success = wifiManager.startScan()
    }
}