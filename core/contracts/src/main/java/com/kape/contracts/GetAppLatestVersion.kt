package com.kape.contracts

import com.privateinternetaccess.account.model.response.LatestClientVersion

fun interface GetAppLatestVersion {
    suspend operator fun invoke(): LatestClientVersion?
}