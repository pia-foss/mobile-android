package com.kape.csi.data

import com.kape.csi.domain.CsiDataSource
import com.privateinternetaccess.csi.CSIAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CsiDataSourceImpl(private val api: CSIAPI) : CsiDataSource {

    override fun send(): Flow<String> = callbackFlow {
        api.send { reportIdentifier, listErrors ->
            if (listErrors.isNotEmpty()) {
                trySend("")
                return@send
            }
            reportIdentifier?.let {
                trySend(it)
            } ?: run {
                trySend("")
            }
        }
        awaitClose { channel.close() }
    }
}