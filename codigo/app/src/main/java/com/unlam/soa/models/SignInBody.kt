package com.unlam.soa.models

data class SignInBody(
                      val email: String,
                      val password: String
) {
    val env: String = "TEST"
    val commission: Int = 1234
    val group: Int = 1234
}
