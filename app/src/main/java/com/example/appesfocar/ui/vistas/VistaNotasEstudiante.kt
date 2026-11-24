package com.example.appesfocar.ui.vistas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.repository.EsfocarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun VistaNotasEstudiante(rutEstudiante: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { EsfocarDatabase.getDatabase(context) }
    val repository = remember { EsfocarRepository(db) }

    var listaEvaluaciones by remember { mutableStateOf(listOf<Rubrica>()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(rutEstudiante) {
        coroutineScope.launch(Dispatchers.IO) {
            val evaluaciones = repository.obtenerEvaluacionesPorRut(rutEstudiante)
            withContext(Dispatchers.Main) {
                listaEvaluaciones = evaluaciones
                cargando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Evaluaciones Registradas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (cargando) {
            Text("Cargando calificaciones...", color = Color.Gray)
        } else if (listaEvaluaciones.isEmpty()) {
            Text(
                text = "No tienes evaluaciones registradas para tu RUT ($rutEstudiante).",
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listaEvaluaciones) { rubrica ->
                    TarjetaEvaluacionEstudiante(rubrica = rubrica)
                }
            }
        }
    }
}

@Composable
fun TarjetaEvaluacionEstudiante(rubrica: Rubrica) {
    var expandido by remember { mutableStateOf(false) }
    val notaMostrar = if (rubrica.notaFinal > 0.0) rubrica.notaFinal else rubrica.nota
    val estadoMostrar = if (rubrica.estado.isNotBlank()) rubrica.estado else if (notaMostrar >= 4.0) "APROBADO" else "REPROBADO"
    val feedbackMostrar = if (rubrica.feedbackDocente.isNotBlank()) rubrica.feedbackDocente else rubrica.feedback

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (rubrica.tituloRubrica.isNotBlank()) rubrica.tituloRubrica else rubrica.titulo,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Toca para ver detalle",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = String.format(Locale.US, "%.2f", notaMostrar),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (notaMostrar >= 4.0) Color(0xFF1B5E20) else Color(0xFFC62828)
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Estado: $estadoMostrar",
                        fontWeight = FontWeight.Bold,
                        color = if (notaMostrar >= 4.0) Color(0xFF1B5E20) else Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Feedback del Instructor:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "\"$feedbackMostrar\"",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
