package com.demo.weatherapplication.domain

import com.demo.weatherapplication.data.models.WeatherModel

interface WeatherRepo {
    suspend fun getCurrentWeatherUseCase(lat: String, long:String, appId :String): WeatherModel
    suspend fun getCityWeatherUseCase(city: String, appId :String): WeatherModel
}