package com.kape.featureflags.domain

fun interface FeatureFlagsDataSource {
    suspend operator fun invoke(): List<String>
}