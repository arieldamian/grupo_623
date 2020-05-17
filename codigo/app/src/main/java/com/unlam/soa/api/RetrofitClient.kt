package com.unlam.soa.api

import com.google.gson.JsonObject
import com.unlam.soa.api.ApiConstants.CONTENT_TYPE_HEADER
import com.unlam.soa.models.SignInBody
import com.unlam.soa.models.UserBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

object ApiConstants {
    const val CONTENT_TYPE_HEADER = "Content-Type:application/json"
}

data class ResponseLogin(
    val state: String,
    val msg: String,
    val token: String
)

interface ApiInterface {
    @Headers(CONTENT_TYPE_HEADER)
    @POST("login")
    fun signin(@Body info: SignInBody): Call<ResponseLogin>

    @Headers(CONTENT_TYPE_HEADER)
    @POST("register")
    fun registerUser(@Body info: UserBody): Call<ResponseBody>
}

class RetrofitInstance {
    companion object {
        private const val BASE_URL: String = "http://so-unlam.net.ar/api/api/"
        //private val dotenv = dotenv()
        // val API_ENV = dotenv["API_ENV"]

        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val client: OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
        }.build()

        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}