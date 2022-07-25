package com.kape.payments.domain

interface BillingDataSource {

    fun loadProducts()

    fun purchaseSelectedProduct(periodOrId: String)
}