package com.kape.payments.di

import com.kape.payments.domain.NoOpPaymentIssueHandler
import com.kape.payments.domain.PaymentIssueHandler
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class PaymentsModule {
    @Singleton(binds = [PaymentIssueHandler::class])
    fun providePaymentIssueHandler(): PaymentIssueHandler = NoOpPaymentIssueHandler()
}