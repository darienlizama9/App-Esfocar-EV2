package com.example.appesfocar.ui.vistas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaRubricas() {
    // Variables para los puntajes/notas de los criterios de la rúbrica
    var criterio1 by remember { mutableStateOf("") }
    var criterio2 by remember { mutableStateOf("") }
    var criterio3 by remember { mutableStateOf("") }
    var criterio4 by remember { mutableStateOf("") }

    // Campo para el feedback cualitativo del profesor
    var comentarioProfesor by remember { mutableStateOf("") }

    // Resultados finales
    var notaFinal by remember { mutableStateOf("") }
    var feedbackEstado by remember { mutableStateOf("") }
    var colorResultado by remember { mutableStateOf(Color.DarkGray) }

    // Scroll para que la pantalla sea cómoda en cualquier celular
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
            // Tarjeta de instrucciones o contexto
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
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
                        text = "Ingrese la calificación o puntaje obtenido en cada uno de los 4 criterios de la rúbrica institucional (Escala de 1.0 a 7.0).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }

            // Campos de Criterios
            OutlinedTextField(
                value = criterio1,
                onValueChange = { criterio1 = it },
                label = { Text("1. Presentación y Portaje (1-7)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = criterio2,
                onValueChange = { criterio2 = it },
                label = { Text("2. Dominio Técnico / Táctico (1-7)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = criterio3,
                onValueChange = { criterio3 = it },
                label = { Text("3. Disciplina y Rigor (1-7)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = criterio4,
                onValueChange = { criterio4 = it },
                label = { Text("4. Liderazgo y Trabajo en Equipo (1-7)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            // Campo para el Feedback Cualitativo del Profesor
            OutlinedTextField(
                value = comentarioProfesor,
                onValueChange = { comentarioProfesor = it },
                label = { Text("Retroalimentación / Feedback del Instructor") },
                placeholder = { Text("Escriba observaciones para el estudiante...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(bottom = 20.dp),
                maxLines = 4
            )

            // Botón de cálculo
            Button(
                onClick = {
                    val n1 = criterio1.toFloatOrNull() ?: 0f
                    val n2 = criterio2.toFloatOrNull() ?: 0f
                    val n3 = criterio3.toFloatOrNull() ?: 0f
                    val n4 = criterio4.toFloatOrNull() ?: 0f

                    // Validamos que no estén vacíos para hacer un promedio real
                    val totalCriterios = listOf(criterio1, criterio2, criterio3, criterio4).count { it.isNotEmpty() }

                    if (totalCriterios > 0) {
                        val promedio = (n1 + n2 + n3 + n4) / totalCriterios

                        // Formato estricto a dos decimales
                        notaFinal = String.format("%.2f", promedio)

                        if (promedio >= 4.0f) {
                            feedbackEstado = "ESTADO: APROBADO"
                            colorResultado = Color(0xFF1B5E20) // Verde institucional
                        } else {
                            feedbackEstado = "ESTADO: REPROBADO"
                            colorResultado = Color(0xFFC62828) // Rojo institucional
                        }
                    } else {
                        notaFinal = "0.00"
                        feedbackEstado = "Por favor ingrese al menos una nota."
                        colorResultado = Color.Gray
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Text("CALCULAR NOTA Y GENERAR REPORTE", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de Resultados (Solo aparece si se calculó algo)
            if (notaFinal.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

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