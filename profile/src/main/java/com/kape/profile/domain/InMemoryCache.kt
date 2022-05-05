package com.kape.profile.domain

import com.kape.profile.data.CacheDatasource

class InMemoryCache : CacheDatasource {
    private val map = mutableMapOf<String, String>()

    override fun getString(key: String): String? {
        return map[key]
    }

    override fun saveString(key: String, value: String) {
        map[key] = value
    }
}