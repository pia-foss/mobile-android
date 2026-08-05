package com.kape.connection.di

import com.kape.connection.domain.GooglePaymentIssueHandlerImpl
import com.kape.connection.domain.PaymentIssueHandler
import com.kape.data.DI
import com.kape.payments.domain.RefreshSubscriptionStatusUseCase
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class PaymentIssueModule {
    @Singleton(binds = [PaymentIssueHandler::class])
    fun providePaymentIssueHandler(
        vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
        refreshSubscriptionStatusUseCase: RefreshSubscriptionStatusUseCase,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): PaymentIssueHandler = GooglePaymentIssueHandlerImpl(vpnSubscriptionPaymentProvider, refreshSubscriptionStatusUseCase, ioScope)
}