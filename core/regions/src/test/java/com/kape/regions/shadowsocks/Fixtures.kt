package com.kape.regions.shadowsocks

import com.kape.data.shadowsocksserver.ShadowsocksServer

internal fun server(
    host: String,
    region: String = "region_$host",
    iso: String = "world",
) = ShadowsocksServer(iso = iso, region = region, host = host, port = 443, key = "shadowsocks", cipher = "aes-128-gcm")