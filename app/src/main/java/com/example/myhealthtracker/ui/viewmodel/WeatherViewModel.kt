package com.example.myhealthtracker.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.remote.WeatherUiModel
import com.example.myhealthtracker.data.repository.WeatherRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    application: Application,
    private val weatherRepository: WeatherRepository
) : AndroidViewModel(application) {

    private val _weather = MutableStateFlow(WeatherUiModel(isLoading = true))
    val weather: StateFlow<WeatherUiModel> = _weather.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(
        application
    )

    init {
        fetchWeather()
    }

    @SuppressLint("MissingPermission")
    fun fetchWeather() {
        viewModelScope.launch {
            _weather.update { it.copy(isLoading = true, error = null) }
            try {
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    null
                ).await()


                // Use actual location or fallback to Nairobi
                val lat = location?.latitude ?: -1.286389
                val lon = location?.longitude ?: 36.817223

                val result = weatherRepository.getWeather(
                    lat = lat,
                    lon = lon
                )
                    result.fold(
                        onSuccess = { weatherData ->
                            _weather.update {
                                weatherData.copy(isLoading = false)
                            }
                        },
                        onFailure = { error ->
                            _weather.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Could not load weather"
                                )
                            }
                        }
                    )

            } catch (e: Exception) {
                _weather.update {
                    it.copy(
                        isLoading = false,
                        error = "Could not load weather"
                    )
                }
            }
        }
    }
}