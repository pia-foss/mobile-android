package com.kape.contracts

import com.kape.data.GenericEndpoint


interface MetaEndpoints {
    fun metaEndpoints(): List<GenericEndpoint>
}