package com.kape.vpnconnect.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.vpnconnect.domain.ConnectionDataSource
import org.koin.android.annotation.KoinWorker
import java.io.IOException

@KoinWorker
class PortForwardingWorker(
    context: Context,
    params: WorkerParameters,
    private val portForwardingUseCase: PortForwardingUseCase,
    private val connectionDataSource: ConnectionDataSource,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            portForwardingUseCase.bindPort(connectionDataSource.getVpnToken())
            Result.success()
        } catch (exception: IOException) {
            Result.failure()
        } catch (exception: IllegalStateException) {
            Result.failure()
        }
    }
}