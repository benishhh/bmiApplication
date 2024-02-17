package com.example.bmiapplication


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.material3.Button

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bmiapplication.ui.theme.BMIApplicationTheme
import androidx.lifecycle.ViewModel


class BMIViewModel : ViewModel() {
    var history = mutableStateListOf<String>()
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BMIApplicationTheme {
                val bmiViewModel: BMIViewModel by viewModels()
                MainScreen(bmiViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: BMIViewModel) {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeScreen(
            onNavigateToCalculator = { currentScreen = "calculator" },
            onNavigateToHistory = { currentScreen = "history" },
            onNavigateToChart = { currentScreen = "chart"},
            onNavigateToInfoScreen = { currentScreen = "info"}
        )
        "calculator" -> BMICalculatorScreen(
            viewModel = viewModel,
            onNavigateToHome = { currentScreen = "home" }
        )
        "history" -> HistoryScreen(
            viewModel = viewModel,
            onNavigateToHome = { currentScreen = "home" }
        )
        "chart" -> BMIChartScreen(
            bmiHistory = viewModel.history.map { it },
            onNavigateToHome = { currentScreen = "home" }
        )
        "info" -> InfoScreen(
            onNavigateToHome = { currentScreen = "home" }
        )
    }
}




@Composable
fun HomeScreen(onNavigateToCalculator: () -> Unit, onNavigateToHistory: () -> Unit, onNavigateToChart: () -> Unit, onNavigateToInfoScreen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Witaj w Kalkulatorze BMI",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToCalculator) {
            Text("Przejdź do Kalkulatora")
        }
        Button(onClick = onNavigateToHistory) {
            Text("Przejdź do Historii BMI")
        }
        Button(onClick = onNavigateToChart) {
            Text("Przejdź do Wykresów BMI")
        }
        Button(onClick = onNavigateToInfoScreen) {
            Text("Informacje")
        }
    }
}


@Composable
fun BMICalculatorScreen(onNavigateToHome: () -> Unit, viewModel: BMIViewModel) {

    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Waga (kg)") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Wzrost (cm)") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            result = calculateBMI(weight.toFloatOrNull() ?: 0f, height.toFloatOrNull() ?: 0f).toString()
            viewModel.history.add(result!!)
        }) {
            Text("Oblicz BMI")
        }
        result?.let {
            Text("Twoje BMI: $it")
        }
        Button(onClick = onNavigateToHome) {
            Text("Powrót do strony głównej")
        }
    }
}

fun calculateBMI(weight: Float, height: Float): Float {
    if (height == 0f || weight == 0f) return 0f
    return weight / ((height / 100) * (height / 100))
}

@Composable
fun HistoryScreen(viewModel: BMIViewModel, onNavigateToHome: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Historia BMI")
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            itemsIndexed(viewModel.history) { index, historyItem ->
                Text(
                    text = "${index + 1}. $historyItem",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Button(onClick = onNavigateToHome) {
            Text("Powrót do strony głównej")
        }
    }
}

@Composable
fun BMIChartScreen(bmiHistory: List<String>, onNavigateToHome: () -> Unit) {
    val maxValue = bmiHistory.maxOfOrNull { it.toFloatOrNull() ?: 0f }
        ?: 1f // Znajdź maksymalną wartość dla skali

    Column {
        Button(onClick = onNavigateToHome) {
            Text("Powrót do strony głównej")
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = canvasWidth / (bmiHistory.size * 2f) // Szerokość każdego słupka

            // Rysowanie osi
            drawLine(
                start = Offset(x = 0f, y = canvasHeight),
                end = Offset(x = canvasWidth, y = canvasHeight),
                color = Color.Black,
                strokeWidth = 5f
            )

            // Rysowanie słupków
            bmiHistory.forEachIndexed { index, bmi ->
                val x = barWidth + (index * barWidth * 2)
                val barHeight = (bmi.toFloatOrNull() ?: 0f) / maxValue * canvasHeight
                drawRect(
                    color = Color.Blue,
                    topLeft = Offset(x, canvasHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }

        }
    }
}

@Composable
fun InfoScreen(onNavigateToHome: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Informacje o BMI\n")
        Text("BMI (Body Mass Index) to wskaźnik, który pozwala ocenić, czy masa ciała jest prawidłowa w stosunku do wzrostu.\n")
        Text("Wartości BMI")
        Text("Niedowaga: poniżej 18.5")
        Text("Norma: 18.5 - 24.9")
        Text("Nadwaga: 25 - 29.9")
        Text("Otyłość: powyżej 30")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToHome) {
            Text("Powrót do strony głównej")
        }
    }
}









