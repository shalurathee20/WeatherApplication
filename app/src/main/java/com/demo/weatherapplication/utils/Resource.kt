package com.demo.weatherapplication.utils

sealed class Resource<T>(val data:T?=null, val message:String?=null){

    class Success<T>(dataSuccess:T?):Resource<T>(data=dataSuccess)

    class Loading<T>(messageLoading:String?):Resource<T>()

    class Error<T>(messageError: String?):Resource<T>(message = messageError)
}