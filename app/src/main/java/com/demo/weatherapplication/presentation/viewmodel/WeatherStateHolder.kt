package com.demo.weatherapplication.presentation.viewmodel

import com.demo.weatherapplication.data.models.WeatherModel

data class WeatherStateHolder(
    val isLoading:Boolean=false,
    val data: WeatherModel?=null,
    val error:String=""
)