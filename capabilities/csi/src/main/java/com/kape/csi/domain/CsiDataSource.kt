package com.kape.csi.domain

import kotlinx.coroutines.flow.Flow

fun interface CsiDataSource {

    fun send(): Flow<String>
}