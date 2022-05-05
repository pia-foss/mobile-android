package com.kape.profile.di

import com.kape.profile.data.ProfileDatasourceImpl
import com.kape.profile.domain.ProfileDatasource
import org.koin.dsl.module

val profileModule = module {
    single<ProfileDatasource> { ProfileDatasourceImpl() }
}