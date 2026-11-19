package com.kape.contracts

import com.kape.data.GenericEndpoint

fun interface MetaEndpointsProvider {
    fun metaEndpoints(): List<GenericEndpoint>
}