package com.kape.networkmanagement.utils

import android.content.Context
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import com.kape.ui.R

class NetworkUtil(private val context: Context) {

    fun getDefaultList(): List<NetworkItem> {
        val openWifi = NetworkItem(
            networkName = context.getString(R.string.nmt_open_wifi),
            networkType = NetworkType.WifiOpen,
            networkBehavior = NetworkBehavior.AlwaysConnect,
            isDefaultForOpen = true,
        )

        val mobileData = NetworkItem(
            networkName = context.getString(R.string.nmt_mobile_data),
            networkType = NetworkType.MobileData,
            networkBehavior = NetworkBehavior.AlwaysConnect,
            isDefaultForMobile = true,
        )

        return listOf(openWifi, mobileData)
    }
}