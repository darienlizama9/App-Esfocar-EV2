package com.example.appesfocar.ui.vistas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.network.RetrofitClient
import com.example.appesfocar.repository.EsfocarRepository
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private data class AsignaturaConEvaluaciones(
    val nombreAsignatura: String,
    val docenteEvaluador: String,
    val promedioAsignatura: Double,
    val evaluaciones: List<Rubrica>
)

@Composable
fun VistaNotasEstudiante(
    rutEstudiante: String,
    evaluacionesPreview: List<Rubrica>? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var listaEvaluaciones by remember { mutableStateOf(evaluacionesPreview ?: emptyList()) }
    var docentesRegistrados by remember {
        mutableStateOf(
            if (evaluacionesPreview != null) {
                listOf("Instr. Carlos Mendoza", "Prof. Elena Rojas")
            } else {
                emptyList()
            }
        )
    }
    var cargando by remember { mutableStateOf(evaluacionesPreview == null) }
    var filtroEstado by remember { mutableStateOf("TODAS") }

    fun cargarNotasYDocentes() {
        if (evaluacionesPreview != null) return
        val db = EsfocarDatabase.getDatabase(context)
        val repository = EsfocarRepository(db)

        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { cargando = true }

            val evaluaciones = repository.obtenerEvaluacionesPorRut(rutEstudiante)
            val docentes = try {
                val resp = RetrofitClient.apiService.obtenerUsuarios()
                val usuarios = if (resp.isSuccessful && resp.body() != null) {
                    resp.body()!!
                } else {
                    db.usuarioDao().obtenerTodosLosUsuarios()
                }
                usuarios.filter {
                    it.rol.equals("Docente", ignoreCase = true) ||
                            it.rol.equals("Carabinero", ignoreCase = true)
                }.map { "Instr. ${it.nombre} ${it.apellido}" }
            } catch (_: Exception) {
                db.usuarioDao().obtenerTodosLosUsuarios()
                    .filter { it.rol.equals("Docente", ignoreCase = true) }
                    .map { "Instr. ${it.nombre} ${it.apellido}" }
            }

            withContext(Dispatchers.Main) {
                listaEvaluaciones = evaluaciones
                docentesRegistrados = docentes
                cargando = false
            }
        }
    }

    LaunchedEffect(rutEstudiante) {
        if (evaluacionesPreview == null) {
            cargarNotasYDocentes()
        }
    }

    val notasNumericas = listaEvaluaciones.map { if (it.notaFinal > 0.0) it.notaFinal else it.nota }.filter { it > 0.0 }
    val promedioEstudiante = if (notasNumericas.isNotEmpty()) notasNumericas.average() else 0.0
    val aprobadas = notasNumericas.count { it >= 4.0 }
    val reprobadas = notasNumericas.count { it < 4.0 }
    val porcentajeAprobacion = if (notasNumericas.isNotEmpty()) aprobadas.toFloat() / notasNumericas.size.toFloat() else 0f

    val promedioAnimado by animateFloatAsState(
        targetValue = promedioEstudiante.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "promedio_estudiante"
    )
    val barraProgresoAnimada by animateFloatAsState(
        targetValue = porcentajeAprobacion,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "progreso_aprobacion_estudiante"
    )

    val listaFiltrada = remember(listaEvaluaciones, filtroEstado) {
        when (filtroEstado) {
            "APROBADAS" -> listaEvaluaciones.filter { (if (it.notaFinal > 0.0) it.notaFinal else it.nota) >= 4.0 }
            "REPROBADAS" -> listaEvaluaciones.filter { (if (it.notaFinal > 0.0) it.notaFinal else it.nota) < 4.0 }
            else -> listaEvaluaciones
        }
    }

    // Agrupar evaluaciones por Asignatura y asignar Docente evaluador
    val asignaturasAgrupadas = remember(listaFiltrada, docentesRegistrados) {
        val docentePorDefecto = docentesRegistrados.firstOrNull() ?: "Cuerpo Docente ESFOCAR"
        listaFiltrada
            .groupBy { it.tituloRubrica.ifBlank { "Evaluación Práctica ESFOCAR" } }
            .entries
            .mapIndexed { index, entry ->
                val notasRamo = entry.value.map { if (it.notaFinal > 0.0) it.notaFinal else it.nota }
                val promedioRamo = if (notasRamo.isNotEmpty()) notasRamo.average() else 0.0
                val docenteAsignado = if (docentesRegistrados.isNotEmpty()) {
                    docentesRegistrados[index % docentesRegistrados.size]
                } else {
                    docentePorDefecto
                }
                AsignaturaConEvaluaciones(
                    nombreAsignatura = entry.key,
                    docenteEvaluador = docenteAsignado,
                    promedioAsignatura = promedioRamo,
                    evaluaciones = entry.value
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F5))
            .padding(16.dp)
    ) {
        // Tarjeta Principal de Rendimiento del Estudiante
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0D3B12), Color(0xFF1B5E20))
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFFA5D6A7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MIS ASIGNATURAS Y NOTAS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA5D6A7),
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "RUT: ${rutEstudiante.ifBlank { "No registrado" }}",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { cargarNotasYDocentes() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "PROMEDIO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.2f", promedioAnimado),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when {
                                            notasNumericas.isEmpty() -> Color.DarkGray
                                            promedioEstudiante >= 4.0 -> Color(0xFF1B5E20)
                                            else -> Color(0xFFC62828)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barraProgresoAnimada.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(Color(0xFFA5D6A7))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Asignaturas: ${asignaturasAgrupadas.size}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "Aprobadas: $aprobadas • Reprobadas: $reprobadas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA5D6A7)
                        )
                    }
                }
            }
        }

        // Chips de filtro rápido
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "TODAS" to "Todas (${listaEvaluaciones.size})",
                "APROBADAS" to "Aprobadas ($aprobadas)",
                "REPROBADAS" to "Reprobadas ($reprobadas)"
            ).forEach { (clave, etiqueta) ->
                FilterChip(
                    selected = filtroEstado == clave,
                    onClick = { filtroEstado = clave },
                    label = { Text(etiqueta, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1B5E20),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1B5E20))
            }
        } else if (asignaturasAgrupadas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (listaEvaluaciones.isEmpty()) {
                        "No tienes asignaturas evaluadas para tu RUT ($rutEstudiante)."
                    } else {
                        "No hay evaluaciones en esta categoría."
                    },
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(asignaturasAgrupadas) { asignatura ->
                    TarjetaAsignaturaEstudiante(asignatura = asignatura)
                }
            }
        }
    }
}

@Composable
private fun TarjetaAsignaturaEstudiante(asignatura: AsignaturaConEvaluaciones) {
    var expandida by remember { mutableStateOf(true) }
    val aprobada = asignatura.promedioAsignatura >= 4.0
    val colorEstado = if (aprobada) Color(0xFF1B5E20) else Color(0xFFC62828)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandida = !expandida },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.padding(10.dp).size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = asignatura.nombreAsignatura,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${asignatura.evaluaciones.size} evaluación(es) • Toca para ${if (expandida) "ocultar" else "ver"}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colorEstado.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f", asignatura.promedioAsignatura),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorEstado,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = expandida) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))

                    // Bloque destacado con el Docente / Instructor que evaluó
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F8E9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color(0xFF1B5E20),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "DOCENTE / INSTRUCTOR EVALUADOR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = asignatura.docenteEvaluador,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    asignatura.evaluaciones.forEachIndexed { idx, eval ->
                        val notaEval = if (eval.notaFinal > 0.0) eval.notaFinal else eval.nota
                        val aprobEval = notaEval >= 4.0
                        val estadoEval = if (eval.estado.isNotBlank()) eval.estado else if (aprobEval) "APROBADO" else "REPROBADO"
                        val feedbackEval = if (eval.feedbackDocente.isNotBlank()) eval.feedbackDocente else eval.feedback

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (aprobEval) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (aprobEval) Color(0xFF1B5E20) else Color(0xFFC62828),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Evaluación #${idx + 1} • $estadoEval",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (aprobEval) Color(0xFF1B5E20) else Color(0xFFC62828)
                                    )
                                }
                                Text(
                                    text = "Nota: ${String.format(Locale.US, "%.2f", notaEval)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            Text(
                                text = "Feedback: \"${feedbackEval.ifBlank { "Sin observaciones adicionales." }}\"",
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(top = 4.dp, start = 21.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaNotasEstudiante() {
    val datosDemo = listOf(
        Rubrica(
            id = 1,
            titulo = "Orden Cerrado y Doctrina",
            tipo = "Práctica",
            nota = 6.5,
            feedback = "Excelente postura y dominio de movimientos.",
            rutAlumno = "12345678-9",
            nombreAlumno = "Juan",
            apellidoAlumno = "Pérez",
            tituloRubrica = "Orden Cerrado y Doctrina",
            notaFinal = 6.5,
            estado = "APROBADO",
            feedbackDocente = "Excelente postura y dominio de movimientos."
        ),
        Rubrica(
            id = 2,
            titulo = "Procedimientos Policiales",
            tipo = "Teórico-Práctico",
            nota = 5.8,
            feedback = "Buen manejo de protocolos, reforzar tiempos de respuesta.",
            rutAlumno = "12345678-9",
            nombreAlumno = "Juan",
            apellidoAlumno = "Pérez",
            tituloRubrica = "Procedimientos Policiales",
            notaFinal = 5.8,
            estado = "APROBADO",
            feedbackDocente = "Buen manejo de protocolos, reforzar tiempos de respuesta."
        )
    )
    AppEsfocarTheme {
        VistaNotasEstudiante(
            rutEstudiante = "12345678-9",
            evaluacionesPreview = datosDemo
        )
    }
}