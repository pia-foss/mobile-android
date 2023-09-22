package com.kape.vpn.provider

import com.privateinternetaccess.csi.CSIEndpoint
import com.privateinternetaccess.csi.IEndPointProvider

private const val CSI_BASE_ENDPOINT = "csi.supreme.tools"
const val CSI_TEAM_IDENTIFIER = "pia_android"

class CsiEndpointProvider : IEndPointProvider {
    override val endpoints: List<CSIEndpoint>
        get() = listOf(CSIEndpoint(CSI_BASE_ENDPOINT, false, false, null))
}