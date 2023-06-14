package com.demo.weatherapplication.domain

import com.demo.weatherapplication.data.models.WeatherModel
import com.demo.weatherapplication.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import javax.inject.Inject

class CityWeatherUseCase @Inject constructor(private val weatherRepo: WeatherRepo) {

    operator fun invoke(city: String, appid :String): Flow<Resource<WeatherModel>> = flow {
        emit(value = Resource.Loading(""))
        try{
            emit(Resource.Success(weatherRepo.getCityWeatherUseCase(city, appid)))
        }catch (e: Exception){
            emit(Resource.Error(e.message.toString()))
        }
    }

}