package com.demo.weatherapplication.domain

import com.demo.weatherapplication.data.network.ApiService
import com.demo.weatherapplication.data.repository.WeatherRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DomainModule{

    @Provides
    fun provideWeatherRepo(apiService : ApiService): WeatherRepo {
        return WeatherRepoImpl(apiService =apiService)
    }
}