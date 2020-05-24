package com.unlam.soa.models

import com.unlam.soa.fitsoa.BuildConfig

data class EventBody(
    val type_events: String,
    val state: String,
    val description: String

) {
    val env: String? = BuildConfig.API_ENV
}