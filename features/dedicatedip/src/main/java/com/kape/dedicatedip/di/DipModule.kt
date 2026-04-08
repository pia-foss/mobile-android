package com.kape.dedicatedip.di

import android.content.Context
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.dedicatedip.data.DipDataSourceImpl
import com.kape.dedicatedip.data.DipSignupRepository
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.domain.FetchSignupDipToken
import com.kape.dedicatedip.domain.GetDipMonthlyPlan
import com.kape.dedicatedip.domain.GetDipSupportedCountries
import com.kape.dedicatedip.domain.GetDipYearlyPlan
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.dedicatedip.domain.ValidateDipSignup
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.payments.SubscriptionPrefs
import com.kape.payments.ui.DipSubscriptionPaymentProvider
import com.kape.payments.ui.VpnSubscriptionPaymentProvider
import com.kape.ui.utils.PriceFormatter
import com.kape.vpnregions.utils.RegionListProvider
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class DipModule {

    @Singleton(binds = [DipDataSource::class])
    fun provideDipDataSource(
        context: Context,
        accountApi: AndroidAccountAPI,
        dipPrefs: DipPrefs,
    ): DipDataSource = DipDataSourceImpl(context, accountApi, dipPrefs)

    @Singleton
    fun provideDipSignupRepository(
        dipDataSource: DipDataSource,
        dipPrefs: DipPrefs,
    ): DipSignupRepository = DipSignupRepository(dipDataSource, dipPrefs)

    @Singleton
    fun provideActivateDipUseCase(dataSource: DipDataSource): ActivateDipUseCase =
        ActivateDipUseCase(dataSource)

    @Singleton
    fun provideRenewDipUseCase(dataSource: DipDataSource): RenewDipUseCase =
        RenewDipUseCase(dataSource)

    @Singleton
    fun provideValidateDipSignup(
        subscriptionPrefs: SubscriptionPrefs,
        dataSource: DipDataSource,
    ): ValidateDipSignup = ValidateDipSignup(subscriptionPrefs, dataSource)

    @Singleton
    fun provideGetDipSupportedCountries(
        dipSignupRepository: DipSignupRepository,
    ): GetDipSupportedCountries = GetDipSupportedCountries(dipSignupRepository)

    @Singleton
    fun provideGetDipMonthlyPlan(
        dipSignupRepository: DipSignupRepository,
        dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
        formatter: PriceFormatter,
    ): GetDipMonthlyPlan =
        GetDipMonthlyPlan(dipSignupRepository, dipSubscriptionPaymentProvider, formatter)

    @Singleton
    fun provideGetDipYearlyPlan(
        dipSignupRepository: DipSignupRepository,
        dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
        formatter: PriceFormatter,
    ): GetDipYearlyPlan =
        GetDipYearlyPlan(dipSignupRepository, dipSubscriptionPaymentProvider, formatter)

    @Singleton
    fun provideFetchSignupDipToken(
        dipDataSource: DipDataSource,
    ): FetchSignupDipToken = FetchSignupDipToken(dipDataSource)

    @KoinViewModel
    fun provideDipViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        activateDipUseCase: ActivateDipUseCase,
        getDipSupportedCountries: GetDipSupportedCountries,
        getDipMonthlyPlan: GetDipMonthlyPlan,
        getDipYearlyPlan: GetDipYearlyPlan,
        validateDipSignup: ValidateDipSignup,
        fetchSignupDipToken: FetchSignupDipToken,
        vpnSubscriptionPaymentProvider: VpnSubscriptionPaymentProvider,
        dipSubscriptionPaymentProvider: DipSubscriptionPaymentProvider,
        dipPrefs: DipPrefs,
        connectionPrefs: ConnectionPrefs,
        buildConfigProvider: BuildConfigProvider,
        connectionManager: ConnectionManager,
    ): DipViewModel = DipViewModel(
        router, regionListProvider, activateDipUseCase, getDipSupportedCountries,
        getDipMonthlyPlan, getDipYearlyPlan, validateDipSignup, fetchSignupDipToken,
        vpnSubscriptionPaymentProvider, dipSubscriptionPaymentProvider, dipPrefs,
        connectionPrefs, buildConfigProvider, connectionManager,
    )
}