package com.kape.splash.domain

import com.privateinternetaccess.account.model.response.LatestClientVersion

interface LatestAppVersionDataSource {
    suspend fun getLatestAppVersion(): LatestClientVersion?
}