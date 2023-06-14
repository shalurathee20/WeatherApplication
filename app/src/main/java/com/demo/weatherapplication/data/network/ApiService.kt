package com.demo.weatherapplication.data.network

import com.demo.weatherapplication.data.models.WeatherModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("weather")
    suspend fun getCurrentWeatherData(
        @Query("lat") lat:String,
        @Query("lon") lon:String,
        @Query("APPID") appid:String
    ): Response<WeatherModel>

    @GET("weather")
    suspend fun getCityWeatherData(
        @Query("q") q:String,
        @Query("appid") appid:String
    ): Response<WeatherModel>

}