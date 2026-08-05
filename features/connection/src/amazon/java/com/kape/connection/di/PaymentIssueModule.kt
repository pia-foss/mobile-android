package com.kape.connection.di

import com.kape.connection.domain.NoOpPaymentIssueHandler
import com.kape.connection.domain.PaymentIssueHandler
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class PaymentIssueModule {
    @Singleton(binds = [PaymentIssueHandler::class])
    fun providePaymentIssueHandler(): PaymentIssueHandler = NoOpPaymentIssueHandler()
}