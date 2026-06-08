package com.kape.dedicatedip.di

import android.content.Context
import com.kape.dedicatedip.data.DipSignupRepository
import com.kape.dedicatedip.domain.DipPurchaseDataSource
import com.kape.dedicatedip.domain.DipPurchaseDataSourceImpl
import com.kape.dedicatedip.domain.DipPurchaseHandler
import com.kape.dedicatedip.domain.DipPurchaseHandlerImpl
import com.kape.dedicatedip.domain.FetchSignupDipToken
import com.kape.dedicatedip.domain.GetDipMonthlyPlan
import com.kape.dedicatedip.domain.GetDipSupportedCountries
import com.kape.dedicatedip.domain.GetDipYearlyPlan
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.dedicatedip.domain.ValidateDipSignup
import com.kape.localprefs.prefs.DipPrefs
import com.kape.payments.prefs.SubscriptionPrefs
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.ui.utils.PriceFormatter
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class DipPurchaseModule {
    @Singleton(binds = [DipPurchaseDataSource::class])
    fun provideDipPurchaseDataSource(
        context: Context,
        accountAPI: AndroidAccountAPI,
    ): DipPurchaseDataSource = DipPurchaseDataSourceImpl(context, accountAPI)

    @Singleton(binds = [DipPurchaseHandler::class])
    fun provideDipPurchaseHandler(
        getDipSupportedCountries: GetDipSupportedCountries,
        getDipMonthlyPlan: GetDipMonthlyPlan,
        getDipYearlyPlan: GetDipYearlyPlan,
        validateDipSignup: ValidateDipSignup,
        fetchSignupDipToken: FetchSignupDipToken,
        renewDipUseCase: RenewDipUseCase,
        vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
        dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
    ): DipPurchaseHandler =
        DipPurchaseHandlerImpl(
            getDipSupportedCountries,
            getDipMonthlyPlan,
            getDipYearlyPlan,
            validateDipSignup,
            fetchSignupDipToken,
            renewDipUseCase,
            vpnSubscriptionPaymentProvider,
            dipSubscriptionPaymentProvider,
        )

    @Singleton
    fun provideDipSignupRepository(
        dipDataSource: DipPurchaseDataSource,
        dipPrefs: DipPrefs,
    ): DipSignupRepository = DipSignupRepository(dipDataSource, dipPrefs)

    @Singleton
    fun provideRenewDipUseCase(dataSource: DipPurchaseDataSource): RenewDipUseCase = RenewDipUseCase(dataSource)

    @Singleton
    fun provideValidateDipSignup(
        subscriptionPrefs: SubscriptionPrefs,
        dataSource: DipPurchaseDataSource,
    ): ValidateDipSignup = ValidateDipSignup(subscriptionPrefs, dataSource)

    @Singleton
    fun provideGetDipSupportedCountries(dipSignupRepository: DipSignupRepository): GetDipSupportedCountries =
        GetDipSupportedCountries(dipSignupRepository)

    @Singleton
    fun provideGetDipMonthlyPlan(
        dipSignupRepository: DipSignupRepository,
        dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
        formatter: PriceFormatter,
    ): GetDipMonthlyPlan = GetDipMonthlyPlan(dipSignupRepository, dipSubscriptionPaymentProvider, formatter)

    @Singleton
    fun provideGetDipYearlyPlan(
        dipSignupRepository: DipSignupRepository,
        dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
        formatter: PriceFormatter,
    ): GetDipYearlyPlan = GetDipYearlyPlan(dipSignupRepository, dipSubscriptionPaymentProvider, formatter)

    @Singleton
    fun provideFetchSignupDipToken(dipDataSource: DipPurchaseDataSource): FetchSignupDipToken = FetchSignupDipToken(dipDataSource)
}