package com.kape.httpclient.domain

fun interface GetWebsiteDownloadLink {
    suspend operator fun invoke(): String
}