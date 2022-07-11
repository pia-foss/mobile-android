package com.kape.region_selection.server

data class ServerResponse(
    private val servers: Map<String, Server>? = null,
    private val info: ServerInfo? = null,
    private val body: String? = null,
) {

    fun isValid(): Boolean {
        return !servers.isNullOrEmpty() && info != null && info.isValid()
    }

}