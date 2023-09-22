package com.kape.vpn.provider

import android.os.Build
import com.kape.csi.CsiPrefs
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.privateinternetaccess.csi.ICSIProvider
import com.privateinternetaccess.csi.ProviderType
import com.privateinternetaccess.csi.ReportType
import java.util.Locale

private const val CSI_APPLICATION_INFORMATION_FILENAME = "application_information"
private const val CSI_DEVICE_INFORMATION_FILENAME = "device_information"
private const val CSI_LAST_KNOWN_EXCEPTION_FILENAME = "last_known_exception"
private const val CSI_PROTOCOL_INFORMATION_FILENAME = "protocol_information"
private const val CSI_REGION_INFORMATION_FILENAME = "regions_information"
private const val CSI_USER_SETTINGS_FILENAME = "user_settings"

class CsiDataProvider(
    private val csiPrefs: CsiPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val userAgent: String,
) {

    init {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            csiPrefs.setLastKnownException(throwable.stackTraceToString())
        }
    }

    val protocolInformationProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_PROTOCOL_INFORMATION_FILENAME
        override val isPersistedData: Boolean
            get() = false
        override val providerType: ProviderType
            get() = ProviderType.PROTOCOL_INFORMATION
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getProtocolInformation()
    }

    val regionInformationProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_REGION_INFORMATION_FILENAME
        override val isPersistedData: Boolean
            get() = true
        override val providerType: ProviderType
            get() = ProviderType.REGION_INFORMATION
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getRegionsInformation()
    }

    val userSettingsProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_USER_SETTINGS_FILENAME
        override val isPersistedData: Boolean
            get() = true
        override val providerType: ProviderType
            get() = ProviderType.USER_SETTINGS
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getUserSettings()
    }

    val lastKnownExceptionProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_LAST_KNOWN_EXCEPTION_FILENAME
        override val isPersistedData: Boolean
            get() = true
        override val providerType: ProviderType
            get() = ProviderType.LAST_KNOWN_EXCEPTION
        override val reportType: ReportType
            get() = ReportType.CRASH
        override val value: String
            get() = getLastKnownException()
    }

    val applicationInformationProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_APPLICATION_INFORMATION_FILENAME
        override val isPersistedData: Boolean
            get() = false
        override val providerType: ProviderType
            get() = ProviderType.APPLICATION_INFORMATION
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getApplicationInformation()
    }

    val deviceInformationProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_DEVICE_INFORMATION_FILENAME
        override val isPersistedData: Boolean
            get() = false
        override val providerType: ProviderType
            get() = ProviderType.DEVICE_INFORMATION
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getDeviceInformation()
    }

    // region private
    private fun getProtocolInformation(): String {
        val activeProtocol = settingsPrefs.getSelectedProtocol()
        val settings = when (activeProtocol) {
            VpnProtocols.WireGuard -> settingsPrefs.getWireGuardSettings()
            VpnProtocols.OpenVPN -> settingsPrefs.getOpenVpnSettings()
        }
        val sb = StringBuilder()
        sb.append("~~ Connection Settings ~~\n")
        sb.append("Connection Type: ${settings.transport}\n")
        sb.append("Port Forwarding: ${settingsPrefs.isPortForwardingEnabled()}\n")
        sb.append("Remote Port: ${settings.port}\n")
        sb.append("OVPN Use Small Packets: ${settings.useSmallPackets}\n")
        sb.append(
            "Wireguard Use Small Packets: ${
                settings.useSmallPackets
            }\n",
        )
        sb.append("Protocol: ${activeProtocol.name}\n")
        // TODO: implement as part of https://polymoon.atlassian.net/browse/PIA-606,
        //  once https://polymoon.atlassian.net/browse/PIA-220 is done
        sb.append("Allow Local Network: ${settingsPrefs.isAllowLocalTrafficEnabled()}\n")
        sb.append("\n~~ Encryption Settings ~~\n\n")

        sb.append("Data Encryption: ${settings.dataEncryption.value}\n")
        sb.append(
            "Data Authentication: " + if (settings.dataEncryption.value.lowercase(
                    Locale.ENGLISH,
                ).contains("gcm")
            ) settings.dataEncryption.value else "",
        ).append("\n")

        sb.append("OpenVPN Handshake: ${settings.handshake}\n")
        sb.append("Wireguard Handshake: ${settings.handshake}\n")
        sb.append("\n~~ App Settings ~~\n\n")
        sb.append("1 click connect: ${settingsPrefs.isConnectOnLaunchEnabled()}\n")
        sb.append("Connect on Boot: ${settingsPrefs.isLaunchOnStartupEnabled()}\n")
        sb.append("Connect on App Updated: ${settingsPrefs.isConnectOnAppUpdateEnabled()}\n")
        sb.append("\n~~~~~ End User Settings ~~~~~\n")
        sb.append("\n~~ VPN Logs ~~\n\n")
        // TODO: implement vpn logs as part of https://polymoon.atlassian.net/browse/PIA-377
        sb.append("\n~~~~~ End VPN Logs ~~~~~\n\n")
        return redactIPsFromString(sb.toString())
    }

    // TODO: implement as part of https://polymoon.atlassian.net/browse/PIA-606
    private fun getRegionsInformation(): String {
        return ""
    }

    // TODO: implement as part of https://polymoon.atlassian.net/browse/PIA-606
    private fun getUserSettings(): String {
        return ""
    }

    private fun getLastKnownException(): String {
        return csiPrefs.getLastKnownException()
    }

    private fun getApplicationInformation(): String {
        return userAgent
    }

    private fun getDeviceInformation(): String {
        val sb = StringBuilder()
        sb.append("OS Version: ${System.getProperty("os.version")}(${Build.VERSION.INCREMENTAL}")
            .append("\n")
        sb.append("API Level: ${Build.VERSION.SDK_INT}").append("\n")
        sb.append("Device: ${Build.DEVICE}").append("\n")
        sb.append("Product: ${Build.PRODUCT}").append("\n")
        sb.append("Model: ${Build.MODEL}").append("\n")
        return sb.toString()
    }

    private fun redactIPsFromString(redact: String): String {
        return redact.replace("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b".toRegex(), "REDACTED")
    }
}