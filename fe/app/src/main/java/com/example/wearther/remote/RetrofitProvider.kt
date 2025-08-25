package com.example.wearther.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // BASE_URL = "http://172.30.1.44:8080/"
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
