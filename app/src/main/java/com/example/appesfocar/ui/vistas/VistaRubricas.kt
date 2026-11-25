package com.example.appesfocar.ui.vistas

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.appesfocar.data.Usuario
import com.example.appesfocar.network.RetrofitClient
import com.example.appesfocar.repository.EsfocarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaRubricas() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { EsfocarDatabase.getDatabase(context) }
    val repository = remember { EsfocarRepository(db) }

    var pestanaActiva by remember { mutableIntStateOf(0) }
    val titulosPestanas = listOf("Subir Nota", "Historial Notas")

    var historialEvaluaciones by remember { mutableStateOf(listOf<Rubrica>()) }
    var cargandoHistorial by remember { mutableStateOf(false) }
    var listaEstudiantesRegistrados by remember { mutableStateOf(listOf<Usuario>()) }

    // Cargar estudiantes registrados desde Backend con respaldo en Room
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val estudiantes = try {
                val resp = RetrofitClient.apiService.obtenerUsuarios()
                if (resp.isSuccessful && resp.body() != null) {
                    val remotos = resp.body()!!
                    remotos.forEach { db.usuarioDao().registrarUsuario(it) }
                    remotos.filter { it.rol.equals("Estudiante", ignoreCase = true) }
                } else {
                    db.usuarioDao().obtenerTodosLosUsuarios()
                        .filter { it.rol.equals("Estudiante", ignoreCase = true) }
                }
            } catch (e: Exception) {
                db.usuarioDao().obtenerTodosLosUsuarios()
                    .filter { it.rol.equals("Estudiante", ignoreCase = true) }
            }
            withContext(Dispatchers.Main) {
                listaEstudiantesRegistrados = estudiantes
            }
        }
    }

    fun recargarHistorial() {
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { cargandoHistorial = true }
            val lista = repository.obtenerTodasLasEvaluaciones()
            withContext(Dispatchers.Main) {
                historialEvaluaciones = lista
                cargandoHistorial = false
            }
        }
    }

    LaunchedEffect(pestanaActiva) {
        if (pestanaActiva == 1) {
            recargarHistorial()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Gestión Docente y Notas", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
                )
                TabRow(
                    selectedTabIndex = pestanaActiva,
                    containerColor = Color(0xFF1B5E20),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pestanaActiva]),
                            color = Color(0xFFA5D6A7),
                            height = 3.dp
                        )
                    }
                ) {
                    titulosPestanas.forEachIndexed { index, titulo ->
                        Tab(
                            selected = pestanaActiva == index,
                            onClick = { pestanaActiva = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Default.RateReview else Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(titulo, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (pestanaActiva) {
                0 -> FormularioSubidaNota(
                    repository = repository,
                    estudiantesDisponibles = listaEstudiantesRegistrados,
                    onNotaSubidaExitosamente = {
                        recargarHistorial()
                    }
                )
                1 -> ListaHistorialEvaluaciones(
                    evaluaciones = historialEvaluaciones,
                    cargando = cargandoHistorial,
                    onRefrescar = { recargarHistorial() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioSubidaNota(
    repository: EsfocarRepository,
    estudiantesDisponibles: List<Usuario>,
    onNotaSubidaExitosamente: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var tituloEvaluacion by remember { mutableStateOf("Evaluación Práctica ESFOCAR") }
    var nombreAlumno by remember { mutableStateOf("") }
    var apellidoAlumno by remember { mutableStateOf("") }
    var rutAlumno by remember { mutableStateOf("") }
    var menuEstudiantesExpandido by remember { mutableStateOf(false) }

    var criterio1 by remember { mutableStateOf("") }
    var criterio2 by remember { mutableStateOf("") }
    var criterio3 by remember { mutableStateOf("") }
    var criterio4 by remember { mutableStateOf("") }
    var comentarioProfesor by remember { mutableStateOf("") }

    var enviando by remember { mutableStateOf(false) }
    var ultimoComprobante by remember { mutableStateOf<Rubrica?>(null) }

    // Filtrar estudiantes registrados según lo que escribe el profesor
    val sugerenciasEstudiantes = remember(nombreAlumno, estudiantesDisponibles) {
        if (nombreAlumno.isBlank()) {
            estudiantesDisponibles
        } else {
            estudiantesDisponibles.filter {
                "${it.nombre} ${it.apellido}".contains(nombreAlumno.trim(), ignoreCase = true) ||
                        it.rut.contains(nombreAlumno.trim(), ignoreCase = true)
            }
        }
    }

    val n1 = criterio1.replace(',', '.').toDoubleOrNull()
    val n2 = criterio2.replace(',', '.').toDoubleOrNull()
    val n3 = criterio3.replace(',', '.').toDoubleOrNull()
    val n4 = criterio4.replace(',', '.').toDoubleOrNull()

    val notasValidas = listOfNotNull(n1, n2, n3, n4).filter { it in 1.0..7.0 }
    val promedioEnVivo = if (notasValidas.isNotEmpty()) {
        BigDecimal(notasValidas.average()).setScale(2, RoundingMode.HALF_UP).toDouble()
    } else {
        0.0
    }

    val promedioAnimado by animateFloatAsState(
        targetValue = promedioEnVivo.toFloat(),
        animationSpec = tween(350),
        label = "promedio_vivo"
    )

    val colorBadge by animateColorAsState(
        targetValue = when {
            promedioEnVivo == 0.0 -> Color(0xFF455A64)
            promedioEnVivo >= 4.0 -> Color(0xFF1B5E20)
            else -> Color(0xFFC62828)
        },
        animationSpec = tween(350),
        label = "color_badge"
    )

    fun esRutValido(rut: String): Boolean {
        val regex = "^[0-9]{7,8}-[0-9kK]$".toRegex()
        return regex.matches(rut)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colorBadge),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PROMEDIO EN TIEMPO REAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = when {
                            promedioEnVivo == 0.0 -> "Ingresa las notas (1.0 - 7.0)"
                            promedioEnVivo >= 4.0 -> "APROBADO (${notasValidas.size}/4 criterios)"
                            else -> "REPROBADO (${notasValidas.size}/4 criterios)"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2f", promedioAnimado),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorBadge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Sección 1: Identificación con Autocompletado de Estudiantes Registrados
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Datos de Evaluación y Alumno", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                    if (estudiantesDisponibles.isNotEmpty()) {
                        Text(
                            text = "${estudiantesDisponibles.size} registrados",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tituloEvaluacion,
                    onValueChange = { tituloEvaluacion = it },
                    label = { Text("Nombre de la Evaluación / Asignatura") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    singleLine = true
                )

                // Campo Nombre con Lista Desplegable de Alumnos Registrados
                ExposedDropdownMenuBox(
                    expanded = menuEstudiantesExpandido && sugerenciasEstudiantes.isNotEmpty(),
                    onExpandedChange = { menuEstudiantesExpandido = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    OutlinedTextField(
                        value = nombreAlumno,
                        onValueChange = {
                            nombreAlumno = it
                            menuEstudiantesExpandido = true
                        },
                        label = { Text("Nombre del Alumno (toca para buscar)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = menuEstudiantesExpandido && sugerenciasEstudiantes.isNotEmpty()
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = menuEstudiantesExpandido && sugerenciasEstudiantes.isNotEmpty(),
                        onDismissRequest = { menuEstudiantesExpandido = false }
                    ) {
                        sugerenciasEstudiantes.forEach { alumno ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${alumno.nombre} ${alumno.apellido}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "RUT: ${alumno.rut.ifBlank { "Sin RUT" }} • ${alumno.correo}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                onClick = {
                                    // Autocompletar todos los campos del estudiante seleccionado
                                    nombreAlumno = alumno.nombre
                                    apellidoAlumno = alumno.apellido
                                    if (alumno.rut.isNotBlank()) {
                                        rutAlumno = alumno.rut
                                    }
                                    menuEstudiantesExpandido = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = apellidoAlumno,
                        onValueChange = { apellidoAlumno = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rutAlumno,
                        onValueChange = { rutAlumno = it },
                        label = { Text("RUT (12345678-9)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Sección 2: Criterios de Evaluación
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Calificaciones por Criterio (Escala 1.0 a 7.0)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = criterio1,
                        onValueChange = { criterio1 = it },
                        label = { Text("1. Porte / Pres.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = criterio2,
                        onValueChange = { criterio2 = it },
                        label = { Text("2. Táctico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = criterio3,
                        onValueChange = { criterio3 = it },
                        label = { Text("3. Disciplina") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = criterio4,
                        onValueChange = { criterio4 = it },
                        label = { Text("4. Liderazgo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = comentarioProfesor,
                    onValueChange = { comentarioProfesor = it },
                    label = { Text("Retroalimentación / Feedback del Instructor") },
                    placeholder = { Text("Observaciones obligatorias para el estudiante...") },
                    modifier = Modifier.fillMaxWidth().height(105.dp),
                    maxLines = 4
                )
            }
        }

        Button(
            enabled = !enviando,
            onClick = {
                if (tituloEvaluacion.isBlank() || nombreAlumno.isBlank() || apellidoAlumno.isBlank() ||
                    rutAlumno.isBlank() || criterio1.isBlank() || criterio2.isBlank() ||
                    criterio3.isBlank() || criterio4.isBlank() || comentarioProfesor.isBlank()
                ) {
                    Toast.makeText(context, "Completa todos los campos, criterios y retroalimentación", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (!esRutValido(rutAlumno.trim())) {
                    Toast.makeText(context, "RUT inválido. Formato esperado: 12345678-9 o 12345678-K", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (n1 == null || n2 == null || n3 == null || n4 == null) {
                    Toast.makeText(context, "Ingresa solo valores numéricos válidos en los criterios", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (n1 !in 1.0..7.0 || n2 !in 1.0..7.0 || n3 !in 1.0..7.0 || n4 !in 1.0..7.0) {
                    Toast.makeText(context, "Cada nota debe estar entre 1.0 y 7.0", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val promedioExacto = BigDecimal((n1 + n2 + n3 + n4) / 4.0)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
                val estadoCalc = if (promedioExacto >= 4.0) "APROBADO" else "REPROBADO"

                enviando = true
                coroutineScope.launch(Dispatchers.IO) {
                    val nuevaRubrica = Rubrica(
                        titulo = "${tituloEvaluacion.trim()} - ${nombreAlumno.trim()} ${apellidoAlumno.trim()}",
                        tipo = "RUT: ${rutAlumno.trim().uppercase()}",
                        nota = promedioExacto,
                        feedback = comentarioProfesor.trim(),
                        rutAlumno = rutAlumno.trim().uppercase(),
                        nombreAlumno = nombreAlumno.trim(),
                        apellidoAlumno = apellidoAlumno.trim(),
                        tituloRubrica = tituloEvaluacion.trim(),
                        notaFinal = promedioExacto,
                        estado = estadoCalc,
                        feedbackDocente = comentarioProfesor.trim()
                    )
                    val guardada = repository.evaluarAlumno(nuevaRubrica)
                    withContext(Dispatchers.Main) {
                        enviando = false
                        ultimoComprobante = guardada
                        Toast.makeText(context, "Nota subida correctamente para ${guardada.nombreAlumno}", Toast.LENGTH_SHORT).show()
                        nombreAlumno = ""
                        apellidoAlumno = ""
                        rutAlumno = ""
                        criterio1 = ""
                        criterio2 = ""
                        criterio3 = ""
                        criterio4 = ""
                        comentarioProfesor = ""
                        onNotaSubidaExitosamente()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
        ) {
            if (enviando) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("SUBIENDO CALIFICACIÓN...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PUBLICAR NOTA AL ESTUDIANTE", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(
            visible = ultimoComprobante != null,
            enter = fadeIn(tween(450)) + expandVertically(tween(450))
        ) {
            ultimoComprobante?.let { comp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1B5E20))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Calificación Publicada",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Alumno: ${comp.nombreAlumno} ${comp.apellidoAlumno} (${comp.rutAlumno})", fontSize = 14.sp)
                        Text("Evaluación: ${comp.tituloRubrica}", fontSize = 14.sp)
                        Text(
                            text = "Nota Final: ${String.format(Locale.US, "%.2f", comp.notaFinal)} — ${comp.estado}",
                            fontWeight = FontWeight.Bold,
                            color = if (comp.notaFinal >= 4.0) Color(0xFF1B5E20) else Color(0xFFC62828),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListaHistorialEvaluaciones(
    evaluaciones: List<Rubrica>,
    cargando: Boolean,
    onRefrescar: () -> Unit
) {
    var filtroBusqueda by remember { mutableStateOf("") }

    val filtradas = remember(evaluaciones, filtroBusqueda) {
        if (filtroBusqueda.isBlank()) {
            evaluaciones
        } else {
            evaluaciones.filter {
                it.nombreAlumno.contains(filtroBusqueda, ignoreCase = true) ||
                        it.apellidoAlumno.contains(filtroBusqueda, ignoreCase = true) ||
                        it.rutAlumno.contains(filtroBusqueda, ignoreCase = true) ||
                        it.tituloRubrica.contains(filtroBusqueda, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = filtroBusqueda,
                onValueChange = { filtroBusqueda = it },
                placeholder = { Text("Buscar por alumno, RUT o ramo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedButton(
                onClick = onRefrescar,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Actualizar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1B5E20))
            }
        } else if (filtradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay evaluaciones registradas aún.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtradas) { item ->
                    TarjetaHistorialDocente(rubrica = item)
                }
            }
        }
    }
}

@Composable
private fun TarjetaHistorialDocente(rubrica: Rubrica) {
    var expandida by remember { mutableStateOf(false) }
    val nota = if (rubrica.notaFinal > 0.0) rubrica.notaFinal else rubrica.nota
    val nombreMostrar = if (rubrica.nombreAlumno.isNotBlank()) {
        "${rubrica.nombreAlumno} ${rubrica.apellidoAlumno}"
    } else {
        rubrica.titulo
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandida = !expandida },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(nombreMostrar, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    Text(
                        text = "${rubrica.tituloRubrica.ifBlank { "Evaluación" }} • RUT: ${rubrica.rutAlumno.ifBlank { rubrica.tipo }}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (nota >= 4.0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2f", nota),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (nota >= 4.0) Color(0xFF1B5E20) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expandida) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        text = "Feedback: \"${rubrica.feedbackDocente.ifBlank { rubrica.feedback }}\"",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
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