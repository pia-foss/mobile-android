package com.kape.profile.data

interface CacheDatasource {

    fun getString(key: String): String?

    fun saveString(key: String, value: String)
}