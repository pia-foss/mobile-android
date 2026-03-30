package com.kape.contracts

import com.kape.contracts.data.GenericEndpoint

interface MetaEndpoints {
    fun metaEndpoints(): List<GenericEndpoint>
}