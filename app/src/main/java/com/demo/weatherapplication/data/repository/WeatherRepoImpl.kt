package com.demo.weatherapplication.data.repository

import com.demo.weatherapplication.data.models.WeatherModel
import com.demo.weatherapplication.data.network.ApiService
import com.demo.weatherapplication.domain.WeatherRepo
import com.demo.weatherapplication.utils.SafeApiRequest
import javax.inject.Inject

class WeatherRepoImpl @Inject constructor(private val apiService: ApiService): WeatherRepo, SafeApiRequest(){

    override suspend fun getCurrentWeatherUseCase(
        lat: String,
        long: String,
        appId: String
    ): WeatherModel {
        val response= safeApiRequest {
            apiService.getCurrentWeatherData(lat,long,appId)
        }
        return response
    }

    override suspend fun getCityWeatherUseCase(city: String, appId: String): WeatherModel {
        val response= safeApiRequest {
            apiService.getCityWeatherData(city,appId)
        }
        return response
    }

}