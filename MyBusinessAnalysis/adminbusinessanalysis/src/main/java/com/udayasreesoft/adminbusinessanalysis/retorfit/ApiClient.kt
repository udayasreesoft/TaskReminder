package com.udayasreesoft.adminbusinessanalysis.retorfit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {
    companion object {

        private var  retrofit : Retrofit? = null
        private val okHttpClient = OkHttpClient.Builder()
            .readTimeout(180, TimeUnit.SECONDS)
            .connectTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()

                // Request customization: add request headers
                val requestBuilder = original.newBuilder()
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Cache-Control", "no-store")

                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        fun getZipCodeApiClient() : Retrofit {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl("http://www.postalpincode.in/api/pincode/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
            }
            return retrofit!!
        }
    }
}