package com.kape.vpn.provider

import com.kape.vpn.utils.STAGING
import com.privateinternetaccess.csi.CSIEndpoint
import com.privateinternetaccess.csi.IEndPointProvider

private const val CSI_BASE_ENDPOINT = "csi.supreme.tools"
const val CSI_TEAM_IDENTIFIER = "pia_android"

class CsiEndpointProvider(private val useStaging: Boolean) : IEndPointProvider {
    override val endpoints: List<CSIEndpoint>
        get() = if (useStaging) {
            listOf(
                CSIEndpoint(
                    STAGING.replace("https://", "").replace("http://", ""),
                    false,
                    false,
                    null,
                ),
            )
        } else {
            listOf(CSIEndpoint(CSI_BASE_ENDPOINT, false, false, null))
        }
}