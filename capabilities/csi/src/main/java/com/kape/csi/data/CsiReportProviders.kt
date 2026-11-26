package com.kape.csi.data

import android.os.Build
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.privateinternetaccess.csi.ICSIProvider
import com.privateinternetaccess.csi.ProviderType
import com.privateinternetaccess.csi.ReportType
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import java.util.Locale

private const val CSI_APPLICATION_INFORMATION_FILENAME = "application_information"
private const val CSI_DEVICE_INFORMATION_FILENAME = "device_information"
private const val CSI_LAST_KNOWN_EXCEPTION_FILENAME = "last_known_exception"
private const val CSI_PROTOCOL_INFORMATION_FILENAME = "protocol_information"
private const val CSI_REGION_INFORMATION_FILENAME = "regions_information"
private const val CSI_USER_SETTINGS_FILENAME = "user_settings"
private const val CSI_PROTOCOL_DEBUG_LOGS_FILENAME = "protocol_debug_logs"

/**
 * The library reads these on its own thread while sending, when nothing collects the prefs'
 * StateFlows, so every value is read straight from storage.
 */
@Singleton
class CsiReportProviders(
    private val csiPrefs: CsiPrefs,
    private val settingsPrefs: SettingsPrefs,
    @Named(Qualifiers.USER_AGENT) private val userAgent: String,
) {
    val protocolInformationProvider =
        object : ICSIProvider {
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

    val regionInformationProvider =
        object : ICSIProvider {
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

    val userSettingsProvider =
        object : ICSIProvider {
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

    val lastKnownExceptionProvider =
        object : ICSIProvider {
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

    val applicationInformationProvider =
        object : ICSIProvider {
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

    val deviceInformationProvider =
        object : ICSIProvider {
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

    val protocolDebugLogsProvider =
        object : ICSIProvider {
            override val filename: String
                get() = CSI_PROTOCOL_DEBUG_LOGS_FILENAME
            override val isPersistedData: Boolean
                get() = false
            override val providerType: ProviderType
                get() = ProviderType.LOGGING_INFORMATION
            override val reportType: ReportType
                get() = ReportType.LOG
            override val value: String
                get() = redactIPsFromString(runBlocking { csiPrefs.getProtocolDebugLogsNow() })
        }

    val all: List<ICSIProvider>
        get() =
            listOf(
                applicationInformationProvider,
                deviceInformationProvider,
                lastKnownExceptionProvider,
                protocolInformationProvider,
                regionInformationProvider,
                userSettingsProvider,
                protocolDebugLogsProvider,
            )

    private fun getProtocolInformation(): String = runBlocking { buildProtocolInformation() }

    private suspend fun buildProtocolInformation(): String {
        val activeProtocol = settingsPrefs.getSelectedProtocolNow()
        val wireGuardSettings = settingsPrefs.getWireGuardSettingsNow()
        val openVpnSettings = settingsPrefs.getOpenVpnSettingsNow()
        val settings =
            when (activeProtocol) {
                VpnProtocols.WireGuard -> wireGuardSettings
                VpnProtocols.OpenVPN -> openVpnSettings
                VpnProtocols.Automatic -> settingsPrefs.getAutoSettingsNow()
            }
        val sb = StringBuilder()
        sb.append("~~ Connection Settings ~~\n")
        sb.append("Connection Type: ${settings.transport}\n")
        sb.append("Port Forwarding: ${settingsPrefs.isPortForwardingEnabledNow()}\n")
        sb.append("Remote Port: ${settings.port}\n")
        sb.append("OVPN Use Small Packets: ${openVpnSettings.useSmallPackets}\n")
        sb.append("Wireguard Use Small Packets: ${wireGuardSettings.useSmallPackets}\n")
        sb.append("Wireguard Use Amnezia: ${wireGuardSettings.useAmnezia}\n")
        sb.append("Protocol: ${activeProtocol.name}\n")
        val mtu =
            when (activeProtocol) {
                VpnProtocols.Automatic -> {
                    val wgMtu = WireGuardSettings(useSmallPackets = settings.useSmallPackets).mtu
                    val ovpnMtu = OpenVpnSettings(useSmallPackets = settings.useSmallPackets).mtu
                    "WireGuard $wgMtu, OpenVPN $ovpnMtu"
                }
                else -> settings.mtu.toString()
            }
        sb.append("MTU: $mtu\n")
        sb.append("\n~~ Blocking Settings ~~\n\n")
        sb.append("MACE: ${settingsPrefs.isMaceEnabledNow()}\n")
        sb.append("Allow Local Network: ${settingsPrefs.isAllowLocalTrafficEnabledNow()}\n")
        sb.append("\n~~ Encryption Settings ~~\n\n")

        sb.append("Data Encryption: ${settings.dataEncryption.value}\n")
        sb
            .append(
                "Data Authentication: " +
                    if (settings.dataEncryption.value
                            .lowercase(
                                Locale.ENGLISH,
                            ).contains("gcm")
                    ) {
                        settings.dataEncryption.value
                    } else {
                        ""
                    },
            ).append("\n")

        sb.append("OpenVPN Handshake: ${openVpnSettings.handshake}\n")
        sb.append("Wireguard Handshake: ${wireGuardSettings.handshake}\n")
        sb.append("\n~~ App Settings ~~\n\n")
        sb.append("1 click connect: ${settingsPrefs.isConnectOnLaunchEnabledNow()}\n")
        sb.append("Connect on Boot: ${settingsPrefs.isLaunchOnBootEnabledNow()}\n")
        sb.append("Connect on App Updated: ${settingsPrefs.isConnectOnAppUpdateEnabledNow()}\n")
        sb.append("\n~~~~~ End User Settings ~~~~~\n")
        return redactIPsFromString(sb.toString())
    }

    private fun getRegionsInformation(): String = ""

    private fun getUserSettings(): String = ""

    private fun getLastKnownException(): String = runBlocking { csiPrefs.getLastKnownExceptionNow() }

    private fun getApplicationInformation(): String = userAgent

    private fun getDeviceInformation(): String {
        val sb = StringBuilder()
        sb
            .append("OS Version: ${System.getProperty("os.version")}(${Build.VERSION.INCREMENTAL})")
            .append("\n")
        sb.append("API Level: ${Build.VERSION.SDK_INT}").append("\n")
        sb.append("Device: ${Build.DEVICE}").append("\n")
        sb.append("Product: ${Build.PRODUCT}").append("\n")
        sb.append("Model: ${Build.MODEL}").append("\n")
        return sb.toString()
    }

    private fun redactIPsFromString(redact: String): String = redact.replace("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b".toRegex(), "REDACTED")
}