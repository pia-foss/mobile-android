package com.kape.network.pinning

import com.kape.network.pinning.SocketProtector
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.SocketFactory

internal class ProtectingSocketFactory(
    private val protector: SocketProtector,
) : SocketFactory() {
    // A new Socket has no native fd until it is bound or connected, and protect() needs one.
    // Binding to an ephemeral port creates it; connect() still works on a bound socket.
    override fun createSocket(): Socket =
        Socket().apply {
            bind(InetSocketAddress(0))
            protector.protect(this)
        }

    override fun createSocket(
        host: String,
        port: Int,
    ): Socket = createSocket().apply { connect(InetSocketAddress(host, port)) }

    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int,
    ): Socket = connectFrom(InetSocketAddress(localHost, localPort), InetSocketAddress(host, port))

    override fun createSocket(
        host: InetAddress,
        port: Int,
    ): Socket = createSocket().apply { connect(InetSocketAddress(host, port)) }

    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): Socket = connectFrom(InetSocketAddress(localAddress, localPort), InetSocketAddress(address, port))

    private fun connectFrom(
        local: InetSocketAddress,
        remote: InetSocketAddress,
    ): Socket =
        Socket().apply {
            bind(local)
            protector.protect(this)
            connect(remote)
        }
}