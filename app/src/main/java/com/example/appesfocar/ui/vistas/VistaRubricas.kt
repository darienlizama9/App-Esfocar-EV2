package com.example.appesfocar.ui.vistas

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.data.Usuario
import com.example.appesfocar.network.PlantillaRubricaDto
import com.example.appesfocar.network.RetrofitClient
import com.example.appesfocar.repository.EsfocarRepository
import com.example.appesfocar.repository.ResultadoSync
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.util.esRutValido
import com.example.appesfocar.util.normalizarRut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

private enum class NivelLogro(
    val sigla: String,
    val etiqueta: String,
    val puntos: Int,
    val color: Color,
    val descripcionGenerica: String
) {
    LOGRADO(
        sigla = "L",
        etiqueta = "Logrado",
        puntos = 4,
        color = Color(0xFF1B5E20),
        descripcionGenerica = "Cumple cabalmente con el estándar institucional esperado, sin errores ni observaciones."
    ),
    MEDIANAMENTE_LOGRADO(
        sigla = "ML",
        etiqueta = "Med. Logrado",
        puntos = 3,
        color = Color(0xFF2E7D32),
        descripcionGenerica = "Cumple con los aspectos esenciales del criterio, presentando observaciones menores."
    ),
    POR_LOGRAR(
        sigla = "PL",
        etiqueta = "Por Lograr",
        puntos = 2,
        color = Color(0xFFEF6C00),
        descripcionGenerica = "Desempeño parcial o irregular; requiere refuerzo y corrección directa del instructor."
    ),
    NO_LOGRADO(
        sigla = "NL",
        etiqueta = "No Logrado",
        puntos = 1,
        color = Color(0xFFC62828),
        descripcionGenerica = "No demuestra la competencia mínima requerida o incumple la normativa básica."
    )
}

private data class CriterioRubrica(
    val nombre: String,
    val descLogrado: String = NivelLogro.LOGRADO.descripcionGenerica,
    val descMedLogrado: String = NivelLogro.MEDIANAMENTE_LOGRADO.descripcionGenerica,
    val descPorLograr: String = NivelLogro.POR_LOGRAR.descripcionGenerica,
    val descNoLogrado: String = NivelLogro.NO_LOGRADO.descripcionGenerica
) {
    fun obtenerDescriptor(nivel: NivelLogro): String = when (nivel) {
        NivelLogro.LOGRADO -> descLogrado
        NivelLogro.MEDIANAMENTE_LOGRADO -> descMedLogrado
        NivelLogro.POR_LOGRAR -> descPorLograr
        NivelLogro.NO_LOGRADO -> descNoLogrado
    }
}

private data class PlantillaRubricaUI(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val criterios: List<CriterioRubrica>,
    val esPorDefecto: Boolean = false
)

private fun PlantillaRubricaDto.aPlantillaUI(): PlantillaRubricaUI {
    val idInt = this.id?.toInt() ?: -1
    val desc = "4 criterios institucionales • Max ${this.puntajeMaximo.toInt()} pts"
    val c1 = if (this.criterio1Nombre.isNotBlank()) this.criterio1Nombre else "1. Presentación y Porte Policial"
    val c2 = if (this.criterio2Nombre.isNotBlank()) this.criterio2Nombre else "2. Dominio Técnico / Táctico"
    val c3 = if (this.criterio3Nombre.isNotBlank()) this.criterio3Nombre else "3. Disciplina y Rigor Normativo"
    val c4 = if (this.criterio4Nombre.isNotBlank()) this.criterio4Nombre else "4. Liderazgo y Trabajo en Equipo"

    return PlantillaRubricaUI(
        id = idInt,
        titulo = this.titulo.ifBlank { "Rúbrica de Evaluación" },
        descripcion = desc,
        criterios = listOf(
            CriterioRubrica(c1),
            CriterioRubrica(c2),
            CriterioRubrica(c3),
            CriterioRubrica(c4)
        ),
        esPorDefecto = false
    )
}

private fun PlantillaRubricaUI.aDto(): PlantillaRubricaDto {
    val c1 = criterios.getOrNull(0)?.nombre ?: "1. Criterio 1"
    val c2 = criterios.getOrNull(1)?.nombre ?: "2. Criterio 2"
    val c3 = criterios.getOrNull(2)?.nombre ?: "3. Criterio 3"
    val c4 = criterios.getOrNull(4)?.nombre ?: "4. Criterio 4"
    val maxPts = (criterios.size * 4).toDouble()
    return PlantillaRubricaDto(
        id = if (id > 0) id.toLong() else null,
        titulo = titulo,
        tipo = "Individual",
        criterio1Nombre = c1,
        criterio2Nombre = c2,
        criterio3Nombre = c3,
        criterio4Nombre = c4,
        puntajeMaximo = if (maxPts > 0) maxPts else 16.0
    )
}

private fun PlantillaRubricaUI.aEntidadRubrica(): Rubrica {
    val dto = this.aDto()
    val empaquetados = "${dto.criterio1Nombre}|${dto.criterio2Nombre}|${dto.criterio3Nombre}|${dto.criterio4Nombre}"
    return Rubrica(
        id = if (esPorDefecto || id <= 0) 0 else id,
        titulo = titulo,
        tipo = "PLANTILLA",
        nota = 4.0,
        feedback = empaquetados,
        tituloRubrica = descripcion,
        plantillaRubricaId = if (id > 0) id.toLong() else null
    )
}

private fun Rubrica.aPlantillaUI(): PlantillaRubricaUI {
    val partes = feedback.split("|").map { it.trim() }
    val c1 = partes.getOrElse(0) { "1. Presentación y Porte Policial" }
    val c2 = partes.getOrElse(1) { "2. Dominio Técnico / Táctico" }
    val c3 = partes.getOrElse(2) { "3. Disciplina y Rigor Normativo" }
    val c4 = partes.getOrElse(3) { "4. Liderazgo y Trabajo en Equipo" }

    return PlantillaRubricaUI(
        id = id,
        titulo = titulo.ifBlank { "Rúbrica Institucional" },
        descripcion = tituloRubrica.ifBlank { "4 criterios institucionales • Escala 4 a 16 pts" },
        criterios = listOf(
            CriterioRubrica(if (c1.isNotBlank()) c1 else "1. Criterio 1"),
            CriterioRubrica(if (c2.isNotBlank()) c2 else "2. Criterio 2"),
            CriterioRubrica(if (c3.isNotBlank()) c3 else "3. Criterio 3"),
            CriterioRubrica(if (c4.isNotBlank()) c4 else "4. Criterio 4")
        ),
        esPorDefecto = false
    )
}

private fun calcularNotaDesdePuntos(puntosObtenidos: Int, puntajeMaximo: Int): Double {
    if (puntajeMaximo <= 0 || puntosObtenidos <= 0) return 1.0
    val proporcion = puntosObtenidos.toDouble() / puntajeMaximo.toDouble()
    val notaBruta = if (proporcion >= 0.60) {
        4.0 + 3.0 * ((proporcion - 0.60) / 0.40)
    } else {
        1.0 + 3.0 * (proporcion / 0.60)
    }
    return BigDecimal(notaBruta.coerceIn(1.0, 7.0))
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaRubricas() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { EsfocarDatabase.getDatabase(context) }
    val repository = remember { EsfocarRepository(db) }

    var pestanaActiva by remember { mutableIntStateOf(0) }
    val titulosPestanas = listOf("Evaluar", "Rúbricas", "Historial")

    var historialEvaluaciones by remember { mutableStateOf(listOf<Rubrica>()) }
    var cargandoHistorial by remember { mutableStateOf(false) }
    var listaEstudiantesRegistrados by remember { mutableStateOf(listOf<Usuario>()) }
    var plantillasDisponibles by remember { mutableStateOf(listOf<PlantillaRubricaUI>()) }

    val plantillaBase = remember {
        PlantillaRubricaUI(
            id = -1,
            titulo = "Rúbrica Práctica ESFOCAR (Estándar)",
            descripcion = "4 criterios institucionales • Escala 4 a 16 pts (60% exigencia)",
            criterios = listOf(
                CriterioRubrica(
                    nombre = "1. Presentación y Porte Policial",
                    descLogrado = "Uniforme impecable, postura marcial reglamentaria y equipo completo sin observaciones.",
                    descMedLogrado = "Presentación general correcta, con detalles menores en uniforme o postura.",
                    descPorLograr = "Descuido visible en tenida o postura; requiere corrección inmediata del instructor.",
                    descNoLogrado = "Incumple gravemente el reglamento de presentación personal y porte institucional."
                ),
                CriterioRubrica(
                    nombre = "2. Dominio Técnico / Táctico",
                    descLogrado = "Ejecuta los procedimientos policiales con precisión, seguridad y total dominio técnico.",
                    descMedLogrado = "Aplica correctamente la técnica general, presentando imprecisiones menores en ejecución.",
                    descPorLograr = "Presenta dudas frecuentes en el procedimiento y requiere guía constante del instructor.",
                    descNoLogrado = "Desconoce la técnica o ejecuta maniobras inseguras fuera de protocolo."
                ),
                CriterioRubrica(
                    nombre = "3. Disciplina y Rigor Normativo",
                    descLogrado = "Acata órdenes con prontitud, respeto jerárquico y estricto apego a la normativa.",
                    descMedLogrado = "Mantiene actitud disciplinada, con leves retrasos o distracciones puntuales.",
                    descPorLograr = "Muestra falta de rigor en el cumplimiento de instrucciones y protocolos.",
                    descNoLogrado = "Incumple órdenes directas o vulnera normas disciplinarias durante la instrucción."
                ),
                CriterioRubrica(
                    nombre = "4. Liderazgo y Trabajo en Equipo",
                    descLogrado = "Coordina eficazmente a su escuadra, toma iniciativa y apoya activamente al equipo.",
                    descMedLogrado = "Colabora adecuadamente con sus pares, aunque con iniciativa limitada.",
                    descPorLograr = "Dificultad para integrarse o comunicarse con el equipo durante el ejercicio.",
                    descNoLogrado = "Actitud individualista o pasiva que entorpece el desempeño de la escuadra."
                )
            ),
            esPorDefecto = true
        )
    }

    fun recargarPlantillas() {
        coroutineScope.launch(Dispatchers.IO) {
            val (mapeadas, deServidor) = try {
                val resp = RetrofitClient.apiService.obtenerPlantillasRubrica()
                if (resp.isSuccessful && resp.body() != null) {
                    val dtos = resp.body()!!
                    dtos.forEach { dto ->
                        db.rubricaDao().guardarRubrica(dto.aPlantillaUI().aEntidadRubrica())
                    }
                    Pair(dtos.map { it.aPlantillaUI() }, true)
                } else {
                    val locales = db.rubricaDao().obtenerTodasLasRubricas().filter { it.tipo == "PLANTILLA" }
                    Pair(locales.map { it.aPlantillaUI() }, false)
                }
            } catch (_: Exception) {
                val locales = db.rubricaDao().obtenerTodasLasRubricas().filter { it.tipo == "PLANTILLA" }
                Pair(locales.map { it.aPlantillaUI() }, false)
            }

            val listaFinal = (listOf(plantillaBase) + mapeadas).distinctBy { it.id }

            withContext(Dispatchers.Main) {
                plantillasDisponibles = listaFinal
            }
        }
    }

    fun recargarHistorial() {
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { cargandoHistorial = true }
            val lista = repository.obtenerTodasLasEvaluaciones()
                .filter { it.tipo != "PLANTILLA" }
            withContext(Dispatchers.Main) {
                historialEvaluaciones = lista
                cargandoHistorial = false
            }
        }
    }

    LaunchedEffect(Unit) {
        plantillasDisponibles = listOf(plantillaBase)
        recargarPlantillas()

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
            } catch (_: Exception) {
                db.usuarioDao().obtenerTodosLosUsuarios()
                    .filter { it.rol.equals("Estudiante", ignoreCase = true) }
            }
            withContext(Dispatchers.Main) {
                listaEstudiantesRegistrados = estudiantes
            }
        }
    }

    LaunchedEffect(pestanaActiva) {
        when (pestanaActiva) {
            0, 1 -> recargarPlantillas()
            2 -> recargarHistorial()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Evaluación por Rúbricas",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
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
                                        imageVector = when (index) {
                                            0 -> Icons.Default.RateReview
                                            1 -> Icons.Default.Rule
                                            else -> Icons.Default.History
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
                0 -> FormularioEvaluacionNiveles(
                    repository = repository,
                    plantillas = plantillasDisponibles.ifEmpty { listOf(plantillaBase) },
                    estudiantesDisponibles = listaEstudiantesRegistrados,
                    onNotaSubidaExitosamente = { recargarHistorial() }
                )
                1 -> GestorPlantillasRubrica(
                    db = db,
                    plantillas = plantillasDisponibles.ifEmpty { listOf(plantillaBase) },
                    onActualizarPlantillas = { recargarPlantillas() }
                )
                2 -> ListaHistorialEvaluaciones(
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
private fun FormularioEvaluacionNiveles(
    repository: EsfocarRepository,
    plantillas: List<PlantillaRubricaUI>,
    estudiantesDisponibles: List<Usuario>,
    onNotaSubidaExitosamente: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedPlantillaId by remember { mutableIntStateOf(plantillas.first().id) }
    val plantillaSeleccionada = plantillas.find { it.id == selectedPlantillaId } ?: plantillas.first()
    var menuPlantillaExpandido by remember { mutableStateOf(false) }

    var nombreAlumno by remember { mutableStateOf("") }
    var apellidoAlumno by remember { mutableStateOf("") }
    var rutAlumno by remember { mutableStateOf("") }
    var menuEstudiantesExpandido by remember { mutableStateOf(false) }

    val seleccionesNiveles = remember(plantillaSeleccionada.id) {
        mutableStateListOf<NivelLogro?>().apply {
            repeat(plantillaSeleccionada.criterios.size) { add(null) }
        }
    }

    var comentarioProfesor by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var ultimoComprobante by remember { mutableStateOf<Rubrica?>(null) }

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

    val criteriosEvaluadosCount = seleccionesNiveles.count { it != null }
    val puntosObtenidos = seleccionesNiveles.sumOf { it?.puntos ?: 0 }
    val puntajeMaximo = plantillaSeleccionada.criterios.size * 4
    val notaCalculada = if (criteriosEvaluadosCount > 0) {
        calcularNotaDesdePuntos(
            puntosObtenidos = puntosObtenidos,
            puntajeMaximo = criteriosEvaluadosCount * 4
        )
    } else {
        0.0
    }

    val notaAnimada by animateFloatAsState(
        targetValue = notaCalculada.toFloat(),
        animationSpec = tween(350),
        label = "nota_niveles"
    )

    val colorBadge by animateColorAsState(
        targetValue = when {
            criteriosEvaluadosCount == 0 -> Color(0xFF455A64)
            notaCalculada >= 4.0 -> Color(0xFF1B5E20)
            else -> Color(0xFFC62828)
        },
        animationSpec = tween(350),
        label = "color_badge_niveles"
    )

    fun esRutValido(rut: String): Boolean {
        return com.example.appesfocar.util.esRutValido(rut)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Marcador Superior en Tiempo Real
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colorBadge),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PUNTAJE Y NOTA EN VIVO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (criteriosEvaluadosCount == 0) {
                            "Selecciona niveles (0/${plantillaSeleccionada.criterios.size})"
                        } else {
                            "$puntosObtenidos / $puntajeMaximo pts • ${if (notaCalculada >= 4.0) "APROBADO" else "REPROBADO"}"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Toca un nivel para ver su descriptor de evaluación",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2f", notaAnimada),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorBadge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 1. Selección de Rúbrica + Datos del Alumno
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
                    text = "1. Seleccionar Rúbrica y Estudiante",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = menuPlantillaExpandido,
                    onExpandedChange = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        menuPlantillaExpandido = it
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    OutlinedTextField(
                        value = plantillaSeleccionada.titulo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rúbrica a Aplicar") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuPlantillaExpandido)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = menuPlantillaExpandido,
                        onDismissRequest = { menuPlantillaExpandido = false }
                    ) {
                        plantillas.forEach { plantilla ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(plantilla.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(plantilla.descripcion, fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedPlantillaId = plantilla.id
                                    menuPlantillaExpandido = false
                                }
                            )
                        }
                    }
                }

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
                        label = { Text("Nombre del Alumno (buscar registrados)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = menuEstudiantesExpandido && sugerenciasEstudiantes.isNotEmpty()
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                menuEstudiantesExpandido = false
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        ),
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
                                        Text("${alumno.nombre} ${alumno.apellido}", fontWeight = FontWeight.Bold)
                                        Text("RUT: ${alumno.rut.ifBlank { "Sin RUT" }}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    nombreAlumno = alumno.nombre
                                    apellidoAlumno = alumno.apellido
                                    if (alumno.rut.isNotBlank()) rutAlumno = alumno.rut
                                    menuEstudiantesExpandido = false
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Right) }
                        )
                    )
                    OutlinedTextField(
                        value = rutAlumno,
                        onValueChange = { rutAlumno = it },
                        label = { Text("RUT (12345678-9)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        )
                    )
                }
            }
        }

        // 2. Evaluación por Niveles de Logro con Descriptor Explicativo
        Text(
            text = "2. Marcar Nivel de Logro por Criterio",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        plantillaSeleccionada.criterios.forEachIndexed { index, criterioObj ->
            val nivelActual = seleccionesNiveles.getOrNull(index)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = criterioObj.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = (nivelActual?.color ?: Color.Gray).copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = if (nivelActual != null) "${nivelActual.puntos} pts" else "0/4 pts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = nivelActual?.color ?: Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4 Botones de Nivel de Logro
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NivelLogro.entries.forEach { nivel ->
                            val seleccionado = nivelActual == nivel
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        if (index < seleccionesNiveles.size) {
                                            seleccionesNiveles[index] = nivel
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (seleccionado) nivel.color else Color(0xFFF5F5F5),
                                border = BorderStroke(
                                    width = if (seleccionado) 1.5.dp else 1.dp,
                                    color = if (seleccionado) nivel.color else Color(0xFFE0E0E0)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = nivel.etiqueta,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (seleccionado) Color.White else Color.DarkGray,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${nivel.puntos} pts",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (seleccionado) Color.White.copy(alpha = 0.9f) else nivel.color
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Recuadro explicativo descriptor
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = (nivelActual?.color ?: Color(0xFF455A64)).copy(alpha = 0.08f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = (nivelActual?.color ?: Color(0xFFB0BEC5)).copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = nivelActual?.color ?: Color(0xFF546E7A),
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                if (nivelActual != null) {
                                    Text(
                                        text = "Criterio para ${nivelActual.etiqueta} (${nivelActual.puntos} pts):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = nivelActual.color
                                    )
                                    Text(
                                        text = criterioObj.obtenerDescriptor(nivelActual),
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Selecciona un nivel arriba para ver el fundamento de evaluación.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF546E7A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Feedback del Instructor
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = comentarioProfesor,
                    onValueChange = { comentarioProfesor = it },
                    label = { Text("Retroalimentación / Observaciones del Instructor") },
                    placeholder = { Text("Detalle fortalezas y aspectos a mejorar...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
                )
            }
        }

        Button(
            enabled = !enviando,
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()

                if (nombreAlumno.isBlank() || apellidoAlumno.isBlank() || rutAlumno.isBlank() || comentarioProfesor.isBlank()) {
                    Toast.makeText(context, "Completa los datos del alumno y la retroalimentación", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if (!esRutValido(rutAlumno.trim())) {
                    Toast.makeText(context, "RUT inválido. Formato esperado: 12345678-9 o 12345678-K", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if (seleccionesNiveles.any { it == null }) {
                    Toast.makeText(context, "Debes seleccionar el nivel de logro en todos los criterios", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val notaFinalExacta = calcularNotaDesdePuntos(puntosObtenidos, puntajeMaximo)
                val estadoCalc = if (notaFinalExacta >= 4.0) "APROBADO" else "REPROBADO"
                val resumenNiveles = plantillaSeleccionada.criterios.mapIndexed { idx, crit ->
                    val niv = seleccionesNiveles[idx]!!
                    "${crit.nombre}: ${niv.etiqueta} (${niv.puntos} pts - ${crit.obtenerDescriptor(niv)})"
                }.joinToString(" | ")

                val p1 = seleccionesNiveles.getOrNull(0)?.puntos?.toDouble() ?: 0.0
                val p2 = seleccionesNiveles.getOrNull(1)?.puntos?.toDouble() ?: 0.0
                val p3 = seleccionesNiveles.getOrNull(2)?.puntos?.toDouble() ?: 0.0
                val p4 = seleccionesNiveles.getOrNull(3)?.puntos?.toDouble() ?: 0.0
                val plantillaId = if (plantillaSeleccionada.id > 0) plantillaSeleccionada.id.toLong() else null

                enviando = true
                coroutineScope.launch(Dispatchers.IO) {
                    val nuevaRubrica = Rubrica(
                        titulo = "${plantillaSeleccionada.titulo} - ${nombreAlumno.trim()} ${apellidoAlumno.trim()}",
                        tipo = "Puntaje: $puntosObtenidos/$puntajeMaximo pts",
                        nota = notaFinalExacta,
                        feedback = "${comentarioProfesor.trim()} [$resumenNiveles]",
                        rutAlumno = normalizarRut(rutAlumno),
                        nombreAlumno = nombreAlumno.trim(),
                        apellidoAlumno = apellidoAlumno.trim(),
                        tituloRubrica = plantillaSeleccionada.titulo,
                        notaFinal = notaFinalExacta,
                        estado = estadoCalc,
                        feedbackDocente = "${comentarioProfesor.trim()}\n\nDetalle de Rúbrica:\n$resumenNiveles",
                        plantillaRubricaId = plantillaId,
                        puntaje1 = p1,
                        puntaje2 = p2,
                        puntaje3 = p3,
                        puntaje4 = p4
                    )
                    val resultadoSync = repository.evaluarAlumno(nuevaRubrica)
                    withContext(Dispatchers.Main) {
                        enviando = false
                        val (rubricaFinal, mensajeSync) = when (resultadoSync) {
                            is ResultadoSync.ExitoServidor -> Pair(resultadoSync.data, resultadoSync.mensaje)
                            is ResultadoSync.GuardadoLocalOffline -> Pair(resultadoSync.data, resultadoSync.mensaje)
                            is ResultadoSync.Error -> Pair(resultadoSync.dataLocal ?: nuevaRubrica, resultadoSync.mensaje)
                        }
                        ultimoComprobante = rubricaFinal
                        Toast.makeText(context, mensajeSync, Toast.LENGTH_LONG).show()
                        nombreAlumno = ""
                        apellidoAlumno = ""
                        rutAlumno = ""
                        comentarioProfesor = ""
                        for (i in seleccionesNiveles.indices) {
                            seleccionesNiveles[i] = null
                        }
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
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("PUBLICANDO EVALUACIÓN...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PUBLICAR EVALUACIÓN ($puntosObtenidos/$puntajeMaximo PTS)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(
            visible = ultimoComprobante != null,
            enter = fadeIn(tween(400)) + expandVertically(tween(400))
        ) {
            ultimoComprobante?.let { comp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1B5E20))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Evaluación Publicada", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Alumno: ${comp.nombreAlumno} ${comp.apellidoAlumno} (${comp.rutAlumno})", fontSize = 13.sp)
                        Text("${comp.tipo} • Nota: ${String.format(Locale.US, "%.2f", comp.notaFinal)} (${comp.estado})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GestorPlantillasRubrica(
    db: EsfocarDatabase,
    plantillas: List<PlantillaRubricaUI>,
    onActualizarPlantillas: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mostrarModalCrear by remember { mutableStateOf(false) }
    var plantillaAEditar by remember { mutableStateOf<PlantillaRubricaUI?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Plantillas de Rúbricas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "Niveles: Logrado (4), Med. Logrado (3), Por Lograr (2), No Logrado (1)",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { mostrarModalCrear = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nueva", fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(plantillas) { plantilla ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plantilla.titulo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = plantilla.descripcion,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            if (!plantilla.esPorDefecto) {
                                Row {
                                    IconButton(onClick = { plantillaAEditar = plantilla }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar rúbrica", tint = Color(0xFF1B5E20))
                                    }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                var exitoRemoto = false
                                                var errorMsg = ""
                                                try {
                                                    val resp = RetrofitClient.apiService.eliminarPlantillaRubrica(plantilla.id.toLong())
                                                    if (resp.isSuccessful) {
                                                        exitoRemoto = true
                                                    } else {
                                                        errorMsg = "Código ${resp.code()}"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMsg = e.localizedMessage ?: "sin conexión"
                                                }

                                                db.rubricaDao().eliminarRubrica(plantilla.aEntidadRubrica())
                                                withContext(Dispatchers.Main) {
                                                    val msg = if (exitoRemoto) "Rúbrica eliminada del Servidor y Room" else "Rúbrica eliminada localmente ($errorMsg)"
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                    onActualizarPlantillas()
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar rúbrica", tint = Color(0xFFC62828))
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "Base",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        plantilla.criterios.forEach { criterio ->
                            Text(
                                text = "• ${criterio.nombre}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarModalCrear) {
        ModalFormularioPlantilla(
            tituloModal = "Crear Nueva Rúbrica",
            plantillaInicial = null,
            onDismiss = { mostrarModalCrear = false },
            onGuardar = { titulo, descripcion, criterios ->
                coroutineScope.launch(Dispatchers.IO) {
                    val cList = criterios.toMutableList()
                    while (cList.size < 4) {
                        val num = cList.size + 1
                        cList.add(CriterioRubrica("Criterio $num"))
                    }

                    val tempUI = PlantillaRubricaUI(
                        id = 0,
                        titulo = titulo.trim(),
                        descripcion = descripcion.trim().ifBlank { "${cList.size} criterios • Escala por niveles de logro" },
                        criterios = cList
                    )
                    val dto = tempUI.aDto()

                    try {
                        val resp = RetrofitClient.apiService.guardarPlantillaRubrica(dto)
                        if (resp.isSuccessful && resp.body() != null) {
                            val creadaDto = resp.body()!!
                            val uiCreada = creadaDto.aPlantillaUI()
                            db.rubricaDao().guardarRubrica(uiCreada.aEntidadRubrica())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Plantilla creada en el Servidor (Render / Neon PostgreSQL)", Toast.LENGTH_LONG).show()
                                mostrarModalCrear = false
                                onActualizarPlantillas()
                            }
                        } else {
                            val errEntidad = tempUI.aEntidadRubrica()
                            db.rubricaDao().guardarRubrica(errEntidad)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Servidor devolvió error (${resp.code()}). Guardada en respaldo local Room.", Toast.LENGTH_LONG).show()
                                mostrarModalCrear = false
                                onActualizarPlantillas()
                            }
                        }
                    } catch (e: Exception) {
                        val errEntidad = tempUI.aEntidadRubrica()
                        db.rubricaDao().guardarRubrica(errEntidad)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Modo Offline: Guardada en respaldo local Room", Toast.LENGTH_LONG).show()
                            mostrarModalCrear = false
                            onActualizarPlantillas()
                        }
                    }
                }
            }
        )
    }

    plantillaAEditar?.let { plantilla ->
        ModalFormularioPlantilla(
            tituloModal = "Editar Rúbrica",
            plantillaInicial = plantilla,
            onDismiss = { plantillaAEditar = null },
            onGuardar = { titulo, descripcion, criterios ->
                coroutineScope.launch(Dispatchers.IO) {
                    val cList = criterios.toMutableList()
                    while (cList.size < 4) {
                        val num = cList.size + 1
                        cList.add(CriterioRubrica("Criterio $num"))
                    }

                    val tempUI = PlantillaRubricaUI(
                        id = plantilla.id,
                        titulo = titulo.trim(),
                        descripcion = descripcion.trim(),
                        criterios = cList
                    )
                    val dto = tempUI.aDto()

                    try {
                        val resp = RetrofitClient.apiService.editarPlantillaRubrica(plantilla.id.toLong(), dto)
                        if (resp.isSuccessful && resp.body() != null) {
                            val editadaDto = resp.body()!!
                            val uiEditada = editadaDto.aPlantillaUI()
                            db.rubricaDao().actualizarRubrica(uiEditada.aEntidadRubrica())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Plantilla actualizada en el Servidor (Render / Neon)", Toast.LENGTH_LONG).show()
                                plantillaAEditar = null
                                onActualizarPlantillas()
                            }
                        } else {
                            db.rubricaDao().actualizarRubrica(tempUI.aEntidadRubrica())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Servidor devolvió error (${resp.code()}). Actualizada localmente en Room.", Toast.LENGTH_LONG).show()
                                plantillaAEditar = null
                                onActualizarPlantillas()
                            }
                        }
                    } catch (e: Exception) {
                        db.rubricaDao().actualizarRubrica(tempUI.aEntidadRubrica())
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Modo Offline: Actualizada localmente en Room", Toast.LENGTH_LONG).show()
                            plantillaAEditar = null
                            onActualizarPlantillas()
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ModalFormularioPlantilla(
    tituloModal: String,
    plantillaInicial: PlantillaRubricaUI?,
    onDismiss: () -> Unit,
    onGuardar: (String, String, List<CriterioRubrica>) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var titulo by remember { mutableStateOf(plantillaInicial?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(plantillaInicial?.descripcion ?: "") }
    var c1 by remember { mutableStateOf(plantillaInicial?.criterios?.getOrNull(0)?.nombre ?: "") }
    var c2 by remember { mutableStateOf(plantillaInicial?.criterios?.getOrNull(1)?.nombre ?: "") }
    var c3 by remember { mutableStateOf(plantillaInicial?.criterios?.getOrNull(2)?.nombre ?: "") }
    var c4 by remember { mutableStateOf(plantillaInicial?.criterios?.getOrNull(3)?.nombre ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tituloModal, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
        text = {
            Column(
                modifier = Modifier
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Nombre de la Rúbrica / Asignatura") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción breve") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Criterios (cada uno vale 1 a 4 pts):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = c1,
                    onValueChange = { c1 = it },
                    label = { Text("Criterio 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = c2,
                    onValueChange = { c2 = it },
                    label = { Text("Criterio 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = c3,
                    onValueChange = { c3 = it },
                    label = { Text("Criterio 3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = c4,
                    onValueChange = { c4 = it },
                    label = { Text("Criterio 4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    val listaCriterios = listOf(c1, c2, c3, c4)
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { nombreCrit ->
                            CriterioRubrica(
                                nombre = nombreCrit,
                                descLogrado = "Domina '$nombreCrit' con excelencia y total rigor institucional.",
                                descMedLogrado = "Aplica '$nombreCrit' de forma adecuada, con detalles menores por pulir.",
                                descPorLograr = "Presenta dificultades en '$nombreCrit'; requiere refuerzo práctico.",
                                descNoLogrado = "No demuestra dominio mínimo en '$nombreCrit' durante la evaluación."
                            )
                        }
                    if (titulo.isBlank() || listaCriterios.isEmpty()) {
                        Toast.makeText(context, "Ingresa el nombre y al menos 1 criterio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onGuardar(
                        titulo.trim(),
                        descripcion.trim().ifBlank { "${listaCriterios.size} criterios • Escala por niveles de logro" },
                        listaCriterios
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Text("Guardar Rúbrica")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onDismiss()
            }) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ListaHistorialEvaluaciones(
    evaluaciones: List<Rubrica>,
    cargando: Boolean,
    onRefrescar: () -> Unit
) {
    var filtroBusqueda by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                placeholder = { Text("Buscar por alumno, RUT o rúbrica...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            )
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onRefrescar()
                },
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
                        text = "${rubrica.tituloRubrica.ifBlank { "Evaluación" }} • ${rubrica.tipo}",
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
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "RUT Alumno: ${rubrica.rutAlumno.ifBlank { "Sin RUT" }}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Feedback del Instructor:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = rubrica.feedbackDocente.ifBlank { rubrica.feedback.ifBlank { "Sin retroalimentación" } },
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaRubricas() {
    AppEsfocarTheme {
        VistaRubricas()
    }
}
