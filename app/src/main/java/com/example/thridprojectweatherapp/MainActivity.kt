package com.example.thridprojectweatherapp

import androidx.compose.ui.platform.LocalConfiguration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.thridprojectweatherapp.ui.theme.ThridProjectWeatherAppTheme
import com.example.weathershaker.WeatherRepository
import com.example.weathershaker.WeatherResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import java.util.Calendar

class MainActivity : ComponentActivity(), SensorEventListener {

    //SENSOR MANAGEMENT
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val ShakeThreshold = 800

    //DATA
    private val cities = listOf(
        CityData("London", "England", 51.5074, -0.1278),
        CityData("Madrid", "Spain", 40.4168, -3.7038),
        CityData("Paris", "France", 48.8566, 2.3522),
        CityData("Tokyo", "Japan", 35.6762, 139.6503),
        CityData("New York", "USA", 40.7128, -74.0060),
        CityData("Berlin", "Germany", 52.5200, 13.4050),
        CityData("Rome", "Italy", 41.9028, 12.4964),
        CityData("Sydney", "Australia", -33.8688, 151.2093),
        CityData("Barcelona", "Spain", 41.3851, 2.1734),
        CityData("Amsterdam", "Netherlands", 52.3676, 4.9041),
        CityData("Varsovia", "Poland", 52.2297, 21.0122),
        CityData("Cracovia", "Poland", 50.0647, 19.9450),
        CityData("Gdańsk", "Poland", 54.3520, 18.6466),
        CityData("Wrocław", "Poland", 51.1079, 17.0385),
        CityData("Poznań", "Poland", 52.4064, 16.9252),
        CityData("Łódź", "Poland", 51.7592, 19.4560),
        CityData("Szczecin", "Poland", 53.4285, 14.5528),
        CityData("Białystok", "Poland", 53.1325, 23.1688),
        CityData("Lublin", "Poland", 51.2465, 22.5684),
        CityData("Katowice", "Poland", 50.2649, 19.0238),
        CityData("Praga", "Czech Republic", 50.0755, 14.4378),
        CityData("Viena", "Austria", 48.2082, 16.3738),
        CityData("Budapest", "Hungary", 47.4979, 19.0402),
    )

    // ==================== UI STATE ====================
    private var currentCity = mutableStateOf(cities[0])
    private var weatherData = mutableStateOf<WeatherResponse?>(null)
    private var isLoading = mutableStateOf(false)
    private var errorMessage = mutableStateOf("")
    private var lastUpdateTime = mutableStateOf("")
    private var showCityDialog = mutableStateOf(false)

    // ==================== LIFECYCLE ====================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeSensor()
        loadWeatherForCity(cities[0])
        setContent {
            ThridProjectWeatherAppTheme {
                WeatherApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerSensorListener()
    }

    override fun onPause() {
        super.onPause()
        unregisterSensorListener()
    }

    // ==================== SENSOR INITIALIZATION ====================
    private fun initializeSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            showToast("This device doesn't have an accelerometer")
        }
    }

    private fun registerSensorListener() {
        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
    }

    // ==================== SENSOR CALLBACKS ====================
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                detectShake(it)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    // ==================== SHAKE DETECTION ====================
    private fun detectShake(event: SensorEvent) {
        val curTime = System.currentTimeMillis()

        if ((curTime - lastUpdate) > 100) {
            val diffTime = curTime - lastUpdate
            lastUpdate = curTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = calculateSpeed(x, y, z, diffTime)

            if (speed > ShakeThreshold) {
                onShakeDetected()
            }

            updateLastPosition(x, y, z)
        }
    }

    private fun calculateSpeed(x: Float, y: Float, z: Float, diffTime: Long): Double {
        return sqrt(
            ((x - lastX) * (x - lastX) +
                    (y - lastY) * (y - lastY) +
                    (z - lastZ) * (z - lastZ)).toDouble()
        ) / diffTime * 10000
    }

    private fun updateLastPosition(x: Float, y: Float, z: Float) {
        lastX = x
        lastY = y
        lastZ = z
    }

    private fun onShakeDetected() {
        val randomCity = cities.random()
        showToast("Loading ${randomCity.name}...")
        loadWeatherForCity(randomCity)
    }

    // ==================== WEATHER DATA LOADING ====================
    private fun loadWeatherForCity(city: CityData) {
        currentCity.value = city
        isLoading.value = true
        errorMessage.value = ""

        lifecycleScope.launch {
            try {
                val weather = WeatherRepository.getWeather(city.lat, city.lon)
                weatherData.value = weather
                lastUpdateTime.value = getCurrentTimeFormatted()
                isLoading.value = false
            } catch (e: Exception) {
                handleLoadingError(e)
            }
        }
    }

    private fun handleLoadingError(e: Exception) {
        errorMessage.value = e.message ?: "Unknown error"
        isLoading.value = false
        showToast("Error loading data: ${e.message}")
    }

    private fun getCurrentTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // ==================== UTILITY ====================
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // ==================== MAIN COMPOSABLE ====================
    @Composable
    fun WeatherApp() {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(createGradientBrush())
        ) {
            if (isLandscape) {
                WeatherAppLandscape()
            } else {
                WeatherAppPortrait()
            }
        }

        if (showCityDialog.value) {
            CitySelectionDialog()
        }
    }

    private fun createGradientBrush(): Brush {
        return Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E1F22),
                Color(0xFF1E1F22),
                Color(0xFF1C1D20)
            )
        )
    }

    // ==================== PORTRAIT LAYOUT ====================
    @Composable
    fun WeatherAppPortrait() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CityHeaderWithBadge(fontSize = 40.sp, topPadding = 48.dp)  // ← Más grande y destacado
            Spacer(modifier = Modifier.height(24.dp))
            WeatherCard()
            Spacer(modifier = Modifier.height(20.dp))
            ShakeInfoCard()  // ← Ahora centrado
            Spacer(modifier = Modifier.height(20.dp))
            ActionButtonsRow()
            Spacer(modifier = Modifier.height(16.dp))
            LastUpdateText()
        }
    }

    // ==================== LANDSCAPE LAYOUT ====================
    @Composable
    fun WeatherAppLandscape() {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Left column: Weather information
            WeatherInfoColumn(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Right column: Controls
            ControlsColumn(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight()
            )
        }
    }
    @Composable
    private fun WeatherInfoColumn(modifier: Modifier) {
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CityHeaderWithBadge(fontSize = 32.sp, topPadding = 32.dp)  // ← Destacado también en landscape
            Spacer(modifier = Modifier.height(16.dp))
            WeatherCard()
            Spacer(modifier = Modifier.height(12.dp))
            LastUpdateText(fontSize = 11.sp)
        }
    }
    @Composable
    private fun ControlsColumn(modifier: Modifier) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShakeInfoCard(fontSize = 14.sp, compact = true)
            Spacer(modifier = Modifier.height(24.dp))
            ActionButtonsColumn()
        }
    }

    // ==================== REUSABLE COMPONENTS ====================
    @Composable
    private fun CityHeaderWithBadge(
        fontSize: androidx.compose.ui.unit.TextUnit = 40.sp,
        topPadding: androidx.compose.ui.unit.Dp = 20.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding)
        ) {
            // Nombre de la ciudad
            Text(
                text = currentCity.value.name,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.fillMaxWidth()
            )

            // País con badge
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                color = Color(0xFF4A90E2),  // Azul vibrante
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentCity.value.country,
                        fontSize = (fontSize.value * 0.45).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }


    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

    @Composable
    private fun ShakeInfoCard(
        fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
        compact: Boolean = false
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 0.dp else 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0x80000000)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally  // ← Centrado
            ) {
                // Icono centrado
                Text(
                    text = if (compact)
                        "Shake your phone\nfor a new city"
                    else
                        "Shake your phone for a new city",
                    fontSize = (fontSize.value * 1.5).sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }

    @Composable
    private fun ActionButtonsRow() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Choose City",
                color = Color(0xFF4A90E2),
                onClick = { showCityDialog.value = true },
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = "Update",
                color = Color(0xFF50C878),
                onClick = { loadWeatherForCity(currentCity.value) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun ActionButtonsColumn() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Choose City",
                color = Color(0xFF4A90E2),
                onClick = { showCityDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            )
            ActionButton(
                text = "Update",
                color = Color(0xFF50C878),
                onClick = { loadWeatherForCity(currentCity.value) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun ActionButton(
        text: String,
        color: Color,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Text(text)
        }
    }

    @Composable
    private fun LastUpdateText(
        fontSize: androidx.compose.ui.unit.TextUnit = 12.sp
    ) {
        Text(
            text = if (lastUpdateTime.value.isNotEmpty())
                "Updated: ${lastUpdateTime.value}"
            else "Loading...",
            fontSize = fontSize,
            color = Color(0xFFCCCCCC),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    // ==================== WEATHER CARD ====================
    @Composable
    fun WeatherCard() {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isLandscape) 8.dp else 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isLandscape) 16.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isLoading.value -> LoadingState(isLandscape)
                    errorMessage.value.isNotEmpty() -> ErrorState(isLandscape)
                    else -> WeatherDataState(isLandscape)
                }
            }
        }
    }

    @Composable
    private fun LoadingState(isLandscape: Boolean) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFFFF6B35)
        )
        Text(
            text = "Loading data of weather...",
            modifier = Modifier.padding(top = 16.dp),
            fontSize = if (isLandscape) 12.sp else 14.sp
        )
    }

    @Composable
    private fun ErrorState(isLandscape: Boolean) {
        Text(
            text = "Error: ${errorMessage.value}",
            color = Color.Red,
            textAlign = TextAlign.Center,
            fontSize = if (isLandscape) 12.sp else 14.sp
        )
    }

    @Composable
    private fun WeatherDataState(isLandscape: Boolean) {
        weatherData.value?.let { weather ->
            val temp = weather.main.temp - 273.15
            val feelsLike = weather.main.feels_like - 273.15
            val weatherEmoji = getWeatherEmoji(
                weather.weather.firstOrNull()?.id ?: 800
            )

            WeatherDisplay(
                emoji = weatherEmoji,
                temp = temp,
                feelsLike = feelsLike,
                description = weather.weather.firstOrNull()?.description ?: "",
                humidity = weather.main.humidity,
                pressure = weather.main.pressure,
                windSpeed = weather.wind.speed,
                isLandscape = isLandscape
            )
        }
    }

    @Composable
    private fun WeatherDisplay(
        emoji: String,
        temp: Double,
        feelsLike: Double,
        description: String,
        humidity: Int,
        pressure: Int,
        windSpeed: Double,
        isLandscape: Boolean
    ) {
        // Emoji
        Text(
            text = emoji,
            fontSize = if (isLandscape) 50.sp else 70.sp,
            modifier = Modifier.padding(bottom = if (isLandscape) 4.dp else 6.dp)
        )

        // Temperature
        Text(
            text = "%.1f°C".format(temp),
            fontSize = if (isLandscape) 48.sp else 72.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35)
        )

        // Description
        Text(
            text = description.replaceFirstChar { it.uppercase() },
            fontSize = if (isLandscape) 16.sp else 20.sp,
            color = Color(0xFF555555),
            modifier = Modifier.padding(top = if (isLandscape) 6.dp else 8.dp)
        )

        // Feels like
        Text(
            text = "Thermal Sensation: %.1f°C".format(feelsLike),
            fontSize = if (isLandscape) 13.sp else 16.sp,
            color = Color(0xFF777777),
            modifier = Modifier.padding(top = if (isLandscape) 12.dp else 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = if (isLandscape) 12.dp else 20.dp),
            color = Color(0xFFE0E0E0)
        )

        // Details row
        WeatherDetailsRow(humidity, pressure, isLandscape)

        Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 12.dp))

        // Wind
        Text(
            text = "Wind: %.1f m/s".format(windSpeed),
            fontSize = if (isLandscape) 12.sp else 14.sp,
            color = Color(0xFF555555)
        )

        // ⭐ NUEVA: Tabla horaria
        HourlyForecastTable(currentTemp = temp, isLandscape = isLandscape)
    }


    @Composable
    private fun WeatherDetailsRow(
        humidity: Int,
        pressure: Int,
        isLandscape: Boolean
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DetailItem("Humidity", "$humidity%", isLandscape)
            DetailItem("Pressure", "$pressure hPa", isLandscape)
        }
    }

    @Composable
    fun DetailItem(label: String, value: String, isLandscape: Boolean = false) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = if (isLandscape) 14.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF555555)
            )
            Text(
                text = label,
                fontSize = if (isLandscape) 11.sp else 12.sp,
                color = Color(0xFF777777)
            )
        }
    }

    @Composable
    private fun HourlyForecastTable(currentTemp: Double, isLandscape: Boolean = false) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Generar datos para las próximas 8 horas
        val hourlyData = (0..7).map { offset ->
            val hour = (currentHour + offset) % 24

            // Variación de temperatura (simulada pero realista)
            val tempVariation = when (hour) {
                in 6..11 -> offset * 0.5  // Mañana: aumenta
                in 12..17 -> 2.0 - (offset * 0.3)  // Tarde: máxima y baja
                in 18..23 -> -offset * 0.4  // Noche: baja
                else -> -2.0 + (offset * 0.2)  // Madrugada: mínima y sube
            }
            val temp = currentTemp + tempVariation

            HourlyForecast(
                hour = String.format("%02d:00", hour),
                temp = temp,
                emoji = getHourlyWeatherEmoji(hour, temp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isLandscape) 12.dp else 16.dp)
        ) {
            // Título de la tabla
            Text(
                text = "Today's Forecast",
                fontSize = if (isLandscape) 16.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Tabla con scroll horizontal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Encabezados
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableHeader("Time", Modifier.weight(1f))
                        TableHeader("Temp", Modifier.weight(1f))
                        TableHeader("Weather", Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFDDDDDD))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Filas con scroll
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = if (isLandscape) 120.dp else 180.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        hourlyData.forEach { forecast ->
                            HourlyForecastRow(forecast, isLandscape)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }


    @Composable
    private fun TableHeader(text: String, modifier: Modifier = Modifier) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = modifier,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun HourlyForecastRow(forecast: HourlyForecast, isLandscape: Boolean) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hora
            Text(
                text = forecast.hour,
                fontSize = if (isLandscape) 13.sp else 14.sp,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Temperatura
            Text(
                text = "%.1f°C".format(forecast.temp),
                fontSize = if (isLandscape) 13.sp else 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF6B35),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // Emoji del clima
            Text(
                text = forecast.emoji,
                fontSize = if (isLandscape) 18.sp else 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }

    // Función para obtener emoji según la hora
    private fun getHourlyWeatherEmoji(hour: Int, temp: Double): String {
        return when {
            hour in 6..8 -> "🌅"  // Amanecer
            hour in 9..11 && temp > 20 -> "🌤️"  // Mañana soleada
            hour in 12..17 && temp > 25 -> "☀️"  // Tarde calurosa
            hour in 12..17 && temp <= 25 -> "⛅"  // Tarde templada
            hour in 18..20 -> "🌆"  // Atardecer
            hour in 21..23 || hour in 0..5 -> "🌙"  // Noche
            else -> "🌤️"
        }
    }

    // ==================== WEATHER EMOJI ====================
    private fun getWeatherEmoji(weatherId: Int): String {
        return when (weatherId) {
            in 200..232 -> "⛈️"  // Thunderstorm
            in 300..321 -> "🌦️"  // Drizzle
            500 -> "🌧️"          // Light rain
            501 -> "🌧️"          // Moderate rain
            502, 503, 504 -> "⛈️" // Heavy rain
            511 -> "🌨️"          // Freezing rain
            in 520..531 -> "🌧️"  // Shower rain
            in 600..622 -> "❄️"  // Snow
            701 -> "🌫️"          // Mist
            711 -> "🌫️"          // Smoke
            721 -> "🌫️"          // Haze
            731, 761 -> "💨"     // Dust/sand
            741 -> "🌫️"          // Fog
            800 -> "☀️"          // Clear
            801 -> "🌤️"          // Few clouds
            802 -> "⛅"          // Scattered clouds
            803 -> "🌥️"          // Broken clouds
            804 -> "☁️"          // Overcast clouds
            else -> "🌍"
        }
    }

    @Composable
    fun CitySelectionDialog() {
        var searchQuery by remember { mutableStateOf("") }
        val filteredCities = getFilteredCities(searchQuery)

        AlertDialog(
            onDismissRequest = { showCityDialog.value = false },
            title = { Text("Choose City") },
            text = {
                CityDialogContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    filteredCities = filteredCities,
                    onCitySelected = { city ->
                        loadWeatherForCity(city)
                        showCityDialog.value = false
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog.value = false }) {
                    Text("Back")
                }
            }
        )
    }

    @Composable
    private fun CityDialogContent(
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
        filteredCities: List<CityData>,
        onCitySelected: (CityData) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
        ) {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            CityList(
                cities = filteredCities,
                onCitySelected = onCitySelected
            )
        }
    }

    @Composable
    private fun SearchBar(
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Search city...") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        )
    }

    @Composable
    private fun CityList(
        cities: List<CityData>,
        onCitySelected: (CityData) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            if (cities.isEmpty()) {
                EmptyCityListMessage()
            } else {
                cities.forEach { city ->
                    CityListItem(
                        city = city,
                        onClick = { onCitySelected(city) }
                    )
                }
            }
        }
    }

    @Composable
    private fun EmptyCityListMessage() {
        Text(
            text = "No cities found",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }

    @Composable
    private fun CityListItem(
        city: CityData,
        onClick: () -> Unit
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = city.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = city.country,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }

    private fun getFilteredCities(query: String): List<CityData> {
        return if (query.isEmpty()) {
            cities
        } else {
            cities.filter { city ->
                city.name.contains(query, ignoreCase = true)
            }
        }
    }
}

// Data class para pronóstico horario
data class HourlyForecast(
    val hour: String,
    val temp: Double,
    val emoji: String
)

data class CityData(
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double
)
