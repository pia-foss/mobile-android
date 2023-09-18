package com.kape.httpclient.domain

import io.ktor.client.HttpClient

interface CertificatePinningClient {

    fun client(): HttpClient

    fun setKnownEndpointCommonName(knownEndpointCommonName: List<Pair<String, String>>)
}