package com.example.myhealthtracker.data.repository

import com.example.myhealthtracker.BuildConfig
import com.example.myhealthtracker.data.remote.WeatherApi
import com.example.myhealthtracker.data.remote.WeatherUiModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi
) {
    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherUiModel> {
        return try {
            val response = weatherApi.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.OPENWEATHER_API_KEY
            )

            val condition = response.weather.firstOrNull()
            val temp = response.main.temp
            val feelsLike = response.main.feelsLike
            val windSpeed = response.wind.speed
            val humidity = response.main.humidity
            val conditionId = condition?.id ?: 800

            val emoji = getWeatherEmoji(conditionId)
            val suggestion = getActivitySuggestion(conditionId, temp, windSpeed)

            Result.success(
                WeatherUiModel(
                    cityName = response.cityName,
                    temperature = "${temp.toInt()}°C",
                    feelsLike = "Feels like ${feelsLike.toInt()}°C",
                    condition = condition?.main ?: "Clear",
                    description = condition?.description
                        ?.replaceFirstChar { it.uppercase() } ?: "",
                    humidity = "💧 ${humidity}%",
                    windSpeed = "💨 ${"%.1f".format(windSpeed)} m/s",
                    emoji = emoji,
                    activitySuggestion = suggestion
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getWeatherEmoji(conditionId: Int): String = when (conditionId) {
        in 200..232 -> "⛈️"  // Thunderstorm
        in 300..321 -> "🌦️"  // Drizzle
        in 500..531 -> "🌧️"  // Rain
        in 600..622 -> "❄️"  // Snow
        in 700..781 -> "🌫️"  // Atmosphere
        800 -> "☀️"           // Clear
        801 -> "🌤️"          // Few clouds
        802 -> "⛅"           // Scattered clouds
        803, 804 -> "☁️"     // Overcast
        else -> "🌤️"
    }

    private fun getActivitySuggestion(
        conditionId: Int,
        temp: Double,
        windSpeed: Double
    ): String = when {
        conditionId in 200..232 -> "⛈️ Stay indoors today"
        conditionId in 300..531 -> "🌧️ Indoor workout recommended"
        conditionId in 600..622 -> "❄️ Be careful — slippery outside"
        conditionId in 700..781 -> "🌫️ Low visibility — stay safe"
        temp > 35 -> "🌡️ Too hot — hydrate well if going out"
        temp < 5 -> "🥶 Cold outside — dress warmly"
        windSpeed > 10 -> "💨 Windy — great for cycling!"
        conditionId == 800 && temp in 15.0..28.0 -> "🏃 Perfect weather for a run!"
        conditionId in 801..802 && temp in 10.0..30.0 -> "👟 Great day for a walk!"
        else -> "💪 Good conditions for exercise!"
    }
}