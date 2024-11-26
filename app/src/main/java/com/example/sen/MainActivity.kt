package com.example.sen

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sen.ui.theme.SenTheme
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.graphicsLayer


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SenTheme {
                // Nawigacja między ekranami, remember aby nie zresetowac
                val navController = rememberNavController()
                Nav(navController)
            }
        }
    }
}

@Composable
fun Nav(navController: NavHostController) {
    // NavHost - kontener zarządzający ekranami
    // NavController przechodzi między ekranami i przekazuje dane
    // startDestination wskazuje pierwszy ekran, który ma się pojawić
    NavHost(navController = navController, startDestination = "splash") {
        // Composable definiuje ekran w systemie nawigacyjnym
        // "mainScreen" to identyfikator ekranu
        // MainScreen(navController) to funkcja odpowiedzialna za ten ekran
        composable("splash") { SplashScreen(navController) }
        composable("mainScreen") { MainScreen(navController) }
        composable("lightSensorScreen") { LightSensorScreen(navController) }
        composable("accelerometerScreen") { AccelerometerScreen(navController) }
        composable("gyroscopeScreen") { GyroscopeScreen(navController) }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    // Efekt uruchamiany tylko raz przy ładowaniu ekranu
    // Unit stała brak danych do obserwacji
    LaunchedEffect(Unit) {
        delay(3000) // Oczekiwanie 3 sekundy
        navController.navigate("mainScreen") // Przenosi do ekranu głównego
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Oliwia Wojdalska 275804",
                color = Color.Black,
                style = TextStyle(fontSize = 32.sp)
            )
            // przestrzen miedzy komponentami
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SENSORY",
                color = Color.Gray,
                style = TextStyle(fontSize = 24.sp)
            )
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { navController.navigate("lightSensorScreen") }) {
                Text("Go to Light Sensor Screen")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("accelerometerScreen") }) {
                Text("Go to Accelerometer Screen")
            }
            // przestrzen miedzy elementami - spacer
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("gyroscopeScreen") }) {
                Text("Go to Gyroscope Screen")
            }
        }
    }
}

@Composable
fun LightSensorScreen(navController: NavHostController) {
    // kontekst aby uzyskać dostęp do zasobów systemowych - czujniki
    val context = LocalContext.current
    // context - informacje o bieżącym stanie aplikacji, dostęp do różnych usług systemowych
    // getSystemService dostęp do usług systemowych, czujniki
    // Context.SENSOR_SERVICE stałą odpowiedzialna za zarządzanie czujnikami w urządzeniu
    // SensorManager dostęp do czujników w Android
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // domysly czujnik swiatla
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    // remember zapamietac a nie zresetowac
    // mutableStateOf(0f) wartość możne zmieniać sie w czasie
    // 0f początkowa wartość zmiennej to 0
    val lightLevel = remember { mutableStateOf(0f) }

    // light float to sa lux z apki i dobrany kolor tla do odpowiednich wartosci
    fun getBackgroundColor(light: Float): Color {
        return when {
            light > 60f -> Color.Yellow // Więcej niż 60 lux - Żółty
            light > 30f -> Color.White  // Więcej niż 30 lux - Biały
            else -> Color.Black // Mniej niż 30 lux - Czarny
        }
    }

    // DisposableEffect pozwala na wykonanie operacji zarządzania zasobami, rejestracja nasłuchiwaczy
    // poki znajdujemy sie na ekranie nasluchwianie a jak wyjdziemy to skok do onDispoe
    // Unit  że będzie uruchamiany raz, niezależnie od jakiejkolwiek zmiennej
    DisposableEffect(Unit) {
        // SensorEventListener pozwala na reagowanie na zmiany w danych z sensorów, nsluchiwanie asynchroniczne zmiany
        val sensorEventListener = object : SensorEventListener {
            // dane z sensora ulegają zmianie. SensorEvent - nowe dane
            override fun onSensorChanged(event: SensorEvent?) {
                // zmiana pochodzi z sensora światła
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    lightLevel.value = event.values[0] // Odczyt wartości światła i zapis do lightlevel
                }
            }
            // zmienia się dokładność sensora, nie wykorzytuje ale budowa narzucona
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        // SensorManager zarządzanie czujnikami w urządzeniu. rejestrowania nasłuchiwaczy i uzyskiwania dostępu do sensorów
        // registerListener() nasłuchiwanie zdarzenia związanego z określonym sensorem
        // SensorEventListener będzie odbierał dane z sensora
        // lightSensor dane na temat natężenia światła w otoczeniu urządzenia
        // SensorManager.SENSOR_DELAY_UI) opóźnienie w odbieraniu danych z sensora
        sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        //onDispose  ekran jest usuwany, użytkownik przechodzi do innej strony
        onDispose {
            // Usuń nasłuchiwacza, ponieważ nie będziemy już potrzebować danych z sensora
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    val backgroundColor = getBackgroundColor(lightLevel.value)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Kolor tła zależny od poziomu światła
            // odstęp wewnętrzny od krawędzi Box
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Tło białe dla tekstu
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp) // Dodanie paddingu wokół tekstu
            ) {
                Text(
                    // tekst z aktualnym poziomem światła, 3 miejsca po przecinku
                    text = "Light Level: ${"%.3f".format(lightLevel.value)} lux",
                    style = TextStyle(fontSize = 24.sp, color = Color.Black)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("mainScreen") }) {
                Text("Go Back")
            }
        }
    }
}



@Composable
fun AccelerometerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val acceleration = remember { mutableStateOf(Triple(0f, 0f, 0f)) }

    // Zmienna do przechowywania x i y
    // z nie bo to przyspiezenie zmienne od x i y
    val position = remember { mutableStateOf(Pair(0f, 0f)) }

    DisposableEffect(Unit) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    // x lewo prawo
                    // y gora dol
                    // z os prostopadla
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    // przechowywanie 3 wartosci bo sa powiazane
                    acceleration.value = Triple(x, y, z)

                    // Przemieszczanie całego układu w zależności od przyspieszenia
                    val horizontalMovement = x * 10 // male wartosci dla x
                    val verticalMovement = -y * 10 // ekran w dol tekst w gore
                    // przechowywanie x i y po ekranie
                    position.value = Pair(horizontalMovement, verticalMovement)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Przesuwamy cały układ na ekranie
        // ofsset przesuwanie lelemntu bez wplywu na uklad rodzica i nie przesuwa el w ukladzie
        Column(
            modifier = Modifier.offset(x = position.value.first.dp, y = position.value.second.dp), // Przemieszczanie całego układu
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Move the device!",
                color = Color.Black,
                style = TextStyle(fontSize = 24.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Acceleration (X, Y, Z):",
                color = Color.Black,
                style = TextStyle(fontSize = 20.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wyświetlanie danych z akcelerometru
            Text(
                text = "X: ${"%.3f".format(acceleration.value.first)} m/s²",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )
            Text(
                text = "Y: ${"%.3f".format(acceleration.value.second)} m/s²",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )
            Text(
                text = "Z: ${"%.3f".format(acceleration.value.third)} m/s²",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("mainScreen") }) {
                Text("Go Back")
            }
        }
    }
}


@Composable
fun GyroscopeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    val rotation = remember { mutableStateOf(Triple(0f, 0f, 0f)) }

    // Zmienna do śledzenia szybkości obrotu
    val rotationSpeed = remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
                    // ogolny wskaznik predkosci to suma predkosci we wszystkich osiach
                    // absoluteValue to wynik bezwzgledny bo obchodzi nas tylko wartosc a nie znak
                    val speed = (event.values[0] + event.values[1] + event.values[2]).absoluteValue
                    // aktualizacja rotacji
                    rotation.value = Triple(event.values[0], event.values[1], event.values[2])

                    // Prędkość obrotu jest używana do zmiany rozmiaru ekranu
                    rotationSpeed.value = speed
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // Obliczanie wielkości ekranu na podstawie prędkości
    // 1f skala 1 do 1 bez powiekszenia/pomnijeszenia
    val scaleFactor = remember { mutableStateOf(1f) }

    // Ustalamy skalowanie w zależności od prędkości obrotu
    // LaunchedEffect Używane do wykonywania działań, zsynchronizowane z jakimś stanem
    LaunchedEffect(rotationSpeed.value) {
        scaleFactor.value = 1f + rotationSpeed.value * 0.5f // Im większa prędkość, tym większy efekt
        // Ograniczamy maksymalną wartość pow zeby byly widoczne dane
        if (scaleFactor.value > 3f) scaleFactor.value = 3f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            // to nam udost skale przezroczystosc itp
            .graphicsLayer {
                // skalowanie po osi x i y
                scaleX = scaleFactor.value
                scaleY = scaleFactor.value
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Gyroscope Data (X, Y, Z):",
                style = TextStyle(fontSize = 24.sp, color = Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "X: ${"%.3f".format(rotation.value.first)} rad/s",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )
            Text(
                text = "Y: ${"%.3f".format(rotation.value.second)} rad/s",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )
            Text(
                text = "Z: ${"%.3f".format(rotation.value.third)} rad/s",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("mainScreen") }) {
                Text("Go Back")
            }
        }
    }
}

