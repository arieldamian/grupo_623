package com.unlam.soa.models

import com.unlam.soa.fitsoa.BuildConfig

data class SignInBody(
    val email: String,
    val password: String
) {
    val env: String? = BuildConfig.API_ENV
    val commission: Int = 1234
    val group: Int = 1234
}
