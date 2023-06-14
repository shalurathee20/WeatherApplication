package com.demo.weatherapplication.data.di

import com.demo.weatherapplication.data.network.ApiService
import com.demo.weatherapplication.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@InstallIn(SingletonComponent::class)
@Module
object DataModule{

    @Provides
    fun provideBaseApiService(): ApiService {
        return Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(
            GsonConverterFactory.create())
            .build().create(ApiService::class.java)
    }

}