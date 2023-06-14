package com.demo.weatherapplication.utils

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.lang.StringBuilder

abstract class SafeApiRequest {

    suspend fun<T: Any> safeApiRequest(call: suspend() -> Response<T>):T{
        val response= call.invoke()
        if (response.isSuccessful) return response.body()!! else{
            val responseError = response.errorBody()?.string()
            val message = StringBuilder()
            responseError.let{
                try{
                    message.append(it?.let { it1 -> JSONObject(it1).getString("error") })
                }catch (_: JSONException){
                }
            }
            Log.d("TAG", "safeApiRequest: $message")

            throw Exception(message.toString())
        }
    }
}