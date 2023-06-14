package com.demo.weatherapplication.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.demo.weatherapplication.R
import com.demo.weatherapplication.data.models.WeatherModel
import com.demo.weatherapplication.databinding.ActivityWeatherBinding
import com.demo.weatherapplication.presentation.viewmodel.CityWeatherViewModel
import com.demo.weatherapplication.presentation.viewmodel.WeatherViewModel
import com.demo.weatherapplication.utils.Constants
import com.demo.weatherapplication.utils.Constants.IMAGE_BASE_URL
import com.demo.weatherapplication.utils.Constants.IMAGE_EXTENSION
import com.demo.weatherapplication.utils.Constants.apiKey
import com.demo.weatherapplication.utils.Utilities
import com.demo.weatherapplication.utils.Utilities.isFirstTimeLaunch
import com.demo.weatherapplication.utils.Utilities.k2c
import com.demo.weatherapplication.utils.Utilities.ts2td
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 101
    private var isLoading = false

    private val mViewModelWeather by viewModels<WeatherViewModel>()
    private val mViewModelCityWeather by viewModels<CityWeatherViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_weather)
        initUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        initUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initUI() {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isFirstTimeLaunch(this@WeatherActivity)) {
                getCurrentLocation()
            } else {
                val city = Utilities.getSp(this@WeatherActivity, Constants.CITY_NAME, "") as String
                if (city.equals("")) {
                    getCurrentLocation()
                } else {
                    getCityWeather(city)
                }
            }
        }

        binding.citySearchEditText.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                getCityWeather(binding.citySearchEditText.text.toString())
                val view = this.currentFocus
                if (view != null) {
                    val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE)
                            as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    binding.citySearchEditText.clearFocus()
                }

                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }

        binding.currentLocationImageView.setOnClickListener {
            getCurrentLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCityWeather(city: String) {
        if (Utilities.isInternetAvailable(this)) {

            if (isLoading) {
                return
            }else{
                mViewModelCityWeather.getCityWeatherData(city, apiKey)
            }

            isLoading = true
            binding.progressBar.visibility = View.VISIBLE

            mViewModelCityWeather.mWeatherList.observe(this@WeatherActivity, Observer {
                isLoading = false
                binding.progressBar.visibility = View.GONE
                if (it != null && it.id > 0) {
                    val city = Utilities.getSp(this@WeatherActivity, Constants.CITY_NAME, "") as String
                    if (it.sys.country.equals("US")) {
                    it.let {
                            setData(it)
                    }
                    }else if(it.name.equals(city)){
                        it.let {
                            setData(it)
                        }
                    }
                } else {
                    val title = resources.getString(R.string.api)
                    val msg = resources.getString(R.string.something_went_wrong)
                    Utilities.showMessageOK(
                        this, title, msg,
                        null
                    )
                    return@Observer
                }
            })
        } else {
            val title = resources.getString(R.string.network)
            val msg = resources.getString(R.string.msg_internet_unavailable)
            Utilities.showMessageOK(
                this, title, msg,
                null
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentLocation() {
        if (Utilities.isInternetAvailable(this)) {
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermission()
                        return
                    }
                    fusedLocationProvider.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                currentLocation = location
                                binding.progressBar.visibility = View.VISIBLE
                                fetchCurrentLocationWeather(
                                    location.latitude.toString(),
                                    location.longitude.toString()
                                )
                            }
                        }
                } else {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            } else {
                requestPermission()
            }
        }else{
            val title = resources.getString(R.string.network)
            val msg = resources.getString(R.string.msg_internet_unavailable)
            Utilities.showMessageOK(
                this, title, msg,
                null
            )
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            LOCATION_REQUEST_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==LOCATION_REQUEST_CODE){

            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                getCurrentLocation()

            }

        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {
        if (Utilities.isInternetAvailable(this)) {
            mViewModelWeather.getCurrentWeatherData(latitude, longitude, apiKey)
            mViewModelWeather.mWeatherList.observe(this@WeatherActivity, Observer {
                binding.progressBar.visibility = View.GONE
                if (it != null && it.id > 0) {
                    it.let {
                        Utilities.setSp(this, Constants.IS_LAUNCHED, true)
                        setData(it)
                    }
                } else {
                    val title = resources.getString(R.string.api)
                    val msg = resources.getString(R.string.something_went_wrong)
                    Utilities.showMessageOK(
                        this, title, msg,
                        null
                    )
                    return@Observer
                }
            })
        } else {
            val title = resources.getString(R.string.network)
            val msg = resources.getString(R.string.msg_internet_unavailable)
            Utilities.showMessageOK(
                this, title, msg,
                null
            )
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData(body: WeatherModel) {

        binding.apply {

            val currentDate = SimpleDateFormat("dd/MM/yyyy hh:mm").format(Date())

            dateTimeTextView.text = currentDate.toString()

            maxTempTextView.text = resources.getString(R.string.max_temp) + " " + k2c(body.main.temp_max) + "°"

            minTempTextView.text = resources.getString(R.string.min_temp) + " " + k2c(body.main.temp_min) + "°"

            tempFeelsLikeTextView.text = "" + k2c(body.main.temp) + "°"

            weatherTypeTextView.text = body.weather[0].main

            sunriseValueTextView.text = ts2td(body.sys.sunrise.toLong())

            sunsetValueTextView.text = ts2td(body.sys.sunset.toLong())

            pressureValueTextView.text = body.main.pressure.toString()

            humidityValueTextView.text = body.main.humidity.toString() + "%"

            tempFTextView.text = "" + (k2c(body.main.temp).times(1.8)).plus(32)
                .roundToInt() + "°"

            citySearchEditText.setText(body.name)

            Utilities.setSp(this@WeatherActivity, Constants.CITY_NAME, body.name)

            val city = Utilities.getSp(this@WeatherActivity, Constants.CITY_NAME, "") as String
            binding.citySearchEditText.setText(city)

            feelsLikeTextView.text =  resources.getString(R.string.feel_like) + " " + k2c(body.main.feels_like) + "°"

            windSpeedTextView.text = body.wind.speed.toString() + "m/s"

            groundValueTextView.text = body.main.grnd_level.toString()

            seaTextView.text = body.main.sea_level.toString()

            countryValueTextView.text = body.sys.country

            Glide.with(this@WeatherActivity)
                .load(IMAGE_BASE_URL + body.weather[0].icon + IMAGE_EXTENSION)
                .override(300, 200)
                .into(weatherImageView)
        }
        updateUI(body.weather[0].id)
    }



    private fun updateUI(id: Int) {
        binding.apply {
            when (id) {
                //Thunderstorm
                in 200..232 -> {
                    mainLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.thunderstrom_bg)
                    weatherOptionsLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.thunderstrom_bg)
                }

                //Drizzle
                in 300..321 -> {
                    mainLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.drizzle_bg)
                    weatherOptionsLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.drizzle_bg)
                }

                //Rain
                in 500..531 -> {
                    mainLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.rain_bg)
                    weatherOptionsLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.rain_bg)
                }

                //Snow
                in 600..622 -> {
                    mainLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.snow_bg)
                    weatherOptionsLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.snow_bg)
                }

                //Atmosphere
                in 701..781 -> {
                    mainLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.atmosphere_bg)
                    weatherOptionsLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.atmosphere_bg)
                }

                //Clear
                800 -> {
                    mainLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.clear_bg)
                    weatherOptionsLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.clear_bg)
                }

                //Clouds
                in 801..804 -> {
                    mainLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.clouds_bg)
                    weatherOptionsLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.clouds_bg)
                }

                //unknown
                else -> {
                    mainLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.unknown_bg)

                    weatherOptionsLayout.background = ContextCompat
                        .getDrawable(this@WeatherActivity, R.drawable.unknown_bg)
                }
            }
        }
    }

}