package com.unlam.soa.api

import com.unlam.soa.api.ApiConstants.BASE_URL
import com.unlam.soa.api.ApiConstants.CONTENT_TYPE_HEADER
import com.unlam.soa.models.SignInBody
import com.unlam.soa.models.UserBody
import com.unlam.soa.sharedPreferences.AppPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

object ApiConstants {
    const val BASE_URL: String = "http://so-unlam.net.ar/api/api/"
    const val CONTENT_TYPE_HEADER = "Content-Type:application/json"
    val CONTENT_TOKEN = "token:" + AppPreferences.token

}

data class ResponseLogin(
    val state: String,
    val msg: String,
    val token: String
)

data class ResponseSignup(
    val state: String,
    val env: String,
    val token: String,
    val msg: String
)

interface ApiInterface {
    @Headers(CONTENT_TYPE_HEADER)
    @POST("login")
    fun signin(@Body info: SignInBody): Call<ResponseLogin>

    @Headers(CONTENT_TYPE_HEADER)
    @POST("register")
    fun registerUser(@Body info: UserBody): Call<ResponseSignup>

    @Headers(CONTENT_TYPE_HEADER)
    @POST("event")
    fun registerEvent(@Body info: UserBody): Call<ResponseSignup>
}

class RetrofitInstance {
    companion object {
        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val client: OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
            this.addInterceptor(ServiceInterceptor())
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

class ServiceInterceptor : Interceptor{

    var token : String = "";
    var path: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        token = AppPreferences.token
        path = request.url().toString()
        if(token.isNotEmpty() && path == BASE_URL+"event")
        {
            request = request.newBuilder()
                .addHeader("token",token)
                .build()
        }

        return chain.proceed(request)
    }

}