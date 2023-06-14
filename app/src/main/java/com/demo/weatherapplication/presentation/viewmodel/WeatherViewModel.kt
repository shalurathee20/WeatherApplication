package com.demo.weatherapplication.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.weatherapplication.data.models.WeatherModel
import com.demo.weatherapplication.domain.CurrentWeatherUseCase
import com.demo.weatherapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(private val getWeatherUseCase: CurrentWeatherUseCase) : ViewModel() {

    var weatherStateStaff = mutableStateOf(WeatherStateHolder())
    var mWeatherList = MutableLiveData<WeatherModel>()

    fun getCurrentWeatherData(lat: String, long:String, appId:String){
        getWeatherUseCase(lat, long, appId).onEach {
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