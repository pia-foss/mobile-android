package com.kape.vpnregions.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class VpnRegionInputStream(private val context: Context) {

    fun readAssetsFile(filename: String): String {
        val inputStream: InputStream = context.assets.open(filename)
        val r = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var receiveString: String?
        while (r.readLine().also { receiveString = it } != null) {
            stringBuilder.append(receiveString).append('\n')
        }
        inputStream.close()
        return stringBuilder.toString()
    }
}