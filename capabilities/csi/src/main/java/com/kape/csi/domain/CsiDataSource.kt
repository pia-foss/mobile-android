package com.kape.csi.domain

fun interface CsiDataSource {
    suspend fun send(): String
}