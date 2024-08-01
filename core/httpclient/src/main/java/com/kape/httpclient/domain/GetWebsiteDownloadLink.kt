package com.kape.httpclient.domain

import kotlinx.coroutines.flow.Flow

fun interface GetWebsiteDownloadLink {
    operator fun invoke(): Flow<String>
}