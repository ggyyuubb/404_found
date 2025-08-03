package com.example.wearther.closet

import com.example.wearther.closet.data.ClosetApi
import com.example.wearther.remote.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClosetService {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("${BASE_URL}api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val api: ClosetApi = retrofit.create(ClosetApi::class.java)
}
