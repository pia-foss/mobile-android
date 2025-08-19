package com.kape.signup.data

interface Identifier {
    operator fun invoke(): Result<String>
}