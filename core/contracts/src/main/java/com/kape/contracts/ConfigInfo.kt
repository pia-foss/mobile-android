package com.kape.contracts

interface ConfigInfo {
    val certificate: String
    val userAgent: String
    val updateUrl: String
    val licences: List<String>
}