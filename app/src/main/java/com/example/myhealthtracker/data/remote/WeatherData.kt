package com.example.myhealthtracker.data.remote

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("main") val main: WeatherMain,
    @SerializedName("wind") val wind: WeatherWind,
    @SerializedName("name") val cityName: String,
    @SerializedName("sys") val sys: WeatherSys
)

data class WeatherCondition(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WeatherMain(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double
)

data class WeatherWind(
    @SerializedName("speed") val speed: Double
)

data class WeatherSys(
    @SerializedName("country") val country: String
)

// UI model
data class WeatherUiModel(
    val cityName: String = "",
    val temperature: String = "",
    val feelsLike: String = "",
    val condition: String = "",
    val description: String = "",
    val humidity: String = "",
    val windSpeed: String = "",
    val emoji: String = "🌤️",
    val activitySuggestion: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)