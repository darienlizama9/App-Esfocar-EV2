package com.example.appesfocar.ui.vistas

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Rubrica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaRubricas() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { EsfocarDatabase.getDatabase(context) }

    // Variables para los puntajes/notas de los criterios
    var criterio1 by remember { mutableStateOf("") }
    var criterio2 by remember { mutableStateOf("") }
    var criterio3 by remember { mutableStateOf("") }
    var criterio4 by remember { mutableStateOf("") }

    // Campo para el feedback cualitativo
    var comentarioProfesor by remember { mutableStateOf("") }

    // Resultados finales
    var notaFinal by remember { mutableStateOf("") }
    var feedbackEstado by remember { mutableStateOf("") }
    var colorResultado by remember { mutableStateOf(Color.DarkGray) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluación y Rúbricas", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de instrucciones
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Instrucciones de Evaluación",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ingrese la calificación obtenida en cada uno de los 4 criterios de la rúbrica institucional (Escala obligatoria de 1.0 a 7.0). Todos los campos son obligatorios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }

            // Campos de Criterios
            OutlinedTextField(
                value = criterio1,
                onValueChange = { criterio1 = it },
                label = { Text("1. Presentación y Porte (1.0 - 7.0)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = criterio2,
                onValueChange = { criterio2 = it },
                label = { Text("2. Dominio Técnico / Táctico (1.0 - 7.0)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = criterio3,
                onValueChange = { criterio3 = it },
                label = { Text("3. Disciplina y Rigor (1.0 - 7.0)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = criterio4,
                onValueChange = { criterio4 = it },
                label = { Text("4. Liderazgo y Trabajo en Equipo (1.0 - 7.0)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            // Campo para el Feedback
            OutlinedTextField(
                value = comentarioProfesor,
                onValueChange = { comentarioProfesor = it },
                label = { Text("Retroalimentación / Feedback del Instructor") },
                placeholder = { Text("Escriba observaciones obligatorias para el estudiante...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(bottom = 20.dp),
                maxLines = 4
            )

            // Botón de cálculo
            Button(
                onClick = {
                    // 1. Validar que no haya campos vacíos
                    if (criterio1.isBlank() || criterio2.isBlank() || criterio3.isBlank() || criterio4.isBlank() || comentarioProfesor.isBlank()) {
                        Toast.makeText(context, "Debes completar los 4 criterios y la retroalimentación", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // 2. Convertir valores
                    val n1 = criterio1.replace(',', '.').toDoubleOrNull()
                    val n2 = criterio2.replace(',', '.').toDoubleOrNull()
                    val n3 = criterio3.replace(',', '.').toDoubleOrNull()
                    val n4 = criterio4.replace(',', '.').toDoubleOrNull()

                    if (n1 == null || n2 == null || n3 == null || n4 == null) {
                        Toast.makeText(context, "No se permiten letras. Ingrese solo números en las notas", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // 3. Validar rango estricto de 1.0 a 7.0
                    if (n1 !in 1.0..7.0 || n2 !in 1.0..7.0 || n3 !in 1.0..7.0 || n4 !in 1.0..7.0) {
                        Toast.makeText(context, "Las notas deben estar obligatoriamente entre 1.0 y 7.0", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // 4. Calcular promedio
                    val promedio = (n1 + n2 + n3 + n4) / 4.0
                    notaFinal = String.format(Locale.US, "%.2f", promedio)

                    if (promedio >= 4.0) {
                        feedbackEstado = "ESTADO: APROBADO"
                        colorResultado = Color(0xFF1B5E20)
                    } else {
                        feedbackEstado = "ESTADO: REPROBADO"
                        colorResultado = Color(0xFFC62828)
                    }

                    // 5. Guardar evaluación en Room Database con la estructura exacta de tu Rubrica.kt
                    coroutineScope.launch(Dispatchers.IO) {
                        val nuevaRubrica = Rubrica(
                            titulo = "Evaluación Institucional",
                            tipo = "Rúbrica Desempeño",
                            nota = promedio,
                            feedback = comentarioProfesor
                        )
                        db.rubricaDao().guardarRubrica(nuevaRubrica)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Calificación registrada con éxito en el sistema", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Text("CALCULAR NOTA Y GENERAR REPORTE", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de Resultados
            if (notaFinal.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CALIFICACIÓN FINAL",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = notaFinal,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResultado
                        )

                        Text(
                            text = feedbackEstado,
                            fontWeight = FontWeight.Bold,
                            color = colorResultado,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        if (comentarioProfesor.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Feedback registrado:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"$comentarioProfesor\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaRubricas() {
    VistaRubricas()
}