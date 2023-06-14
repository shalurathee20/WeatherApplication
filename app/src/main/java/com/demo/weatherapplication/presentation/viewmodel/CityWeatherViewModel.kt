package com.demo.weatherapplication.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.weatherapplication.data.models.WeatherModel
import com.demo.weatherapplication.domain.CityWeatherUseCase
import com.demo.weatherapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CityWeatherViewModel @Inject constructor(private val getWeatherUseCase: CityWeatherUseCase) : ViewModel() {

    var weatherStateStaff = mutableStateOf(WeatherStateHolder())
    var mWeatherList = MutableLiveData<WeatherModel>()

    fun getCityWeatherData(city: String, appId:String){
        getWeatherUseCase(city, appId).onEach {
            when(it){
                is Resource.Loading->{
                    weatherStateStaff.value = WeatherStateHolder(isLoading = true)
                }
                is Resource.Success->{
                    mWeatherList.postValue((it.data))
                    weatherStateStaff.value = WeatherStateHolder(data = it.data)
                }
                is Resource.Error->{
                    weatherStateStaff.value = WeatherStateHolder(error = it.message.toString())
                }
            }
        }.launchIn(viewModelScope)
    }
}