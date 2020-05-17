package com.unlam.soa.models

import com.unlam.soa.fitsoa.BuildConfig

data class UserBody(
                    val name: String,
                    val lastname: String,
                    val dni: Int,
                    val email: String,
                    val password: String
) {
    val env: String? = BuildConfig.API_ENV
    val commission: Int = 1234
    val group: Int = 1234
}
