package com.kape.utils

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class KoverIgnore(val reason: String)