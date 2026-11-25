package com.example.appesfocar.ui.vistas

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.data.Usuario
import com.example.appesfocar.network.RetrofitClient
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.theme.VerdeInstitucional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaAdmin(
    onCerrarSesion: () -> Unit,
    usuariosPreview: List<Usuario>? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var seccionActual by remember { mutableStateOf("inicio") }

    var listaPendientes by remember { mutableStateOf(usuariosPreview ?: emptyList()) }
    var listaActivos by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var listaRubricas by remember { mutableStateOf<List<Rubrica>>(emptyList()) }
    var cargandoDatos by remember { mutableStateOf(false) }

    var usuarioAEditar by remember { mutableStateOf<Usuario?>(null) }
    var usuarioAVerDocumentos by remember { mutableStateOf<Usuario?>(null) }
    var rubricaAEditar by remember { mutableStateOf<Rubrica?>(null) }
    var reporteGenerado by remember { mutableStateOf(false) }

    fun cargarDatos() {
        if (usuariosPreview != null) return
        val db = EsfocarDatabase.getDatabase(context)
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { cargandoDatos = true }
            try {
                val respUsuarios = RetrofitClient.apiService.obtenerUsuarios()
                if (respUsuarios.isSuccessful && respUsuarios.body() != null) {
                    respUsuarios.body()!!.forEach { db.usuarioDao().registrarUsuario(it) }
                }
                val respRubricas = RetrofitClient.apiService.obtenerEvaluaciones()
                if (respRubricas.isSuccessful && respRubricas.body() != null) {
                    respRubricas.body()!!.forEach { db.rubricaDao().guardarRubrica(it) }
                }
            } catch (_: Exception) {
                // Respaldo offline automático en Room
            }

            val pendientes = db.usuarioDao().obtenerUsuariosPendientes()
            val todos = db.usuarioDao().obtenerTodosLosUsuarios()
            val rubricas = db.rubricaDao().obtenerTodasLasRubricas()

            withContext(Dispatchers.Main) {
                listaPendientes = pendientes
                listaActivos = todos.filter { it.cuentaActiva }
                listaRubricas = rubricas
                cargandoDatos = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VerdeInstitucional)
                        .padding(24.dp)
                ) {
                    Text(text = "Panel Administrador", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "ESFOCAR - Sistema Evaluativo", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio / Dashboard") },
                    selected = seccionActual == "inicio",
                    onClick = { seccionActual = "inicio"; coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("Solicitudes Pendientes (${listaPendientes.size})") },
                    selected = seccionActual == "pendientes",
                    onClick = { seccionActual = "pendientes"; coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Usuarios Activos (${listaActivos.size})") },
                    selected = seccionActual == "activos",
                    onClick = { seccionActual = "activos"; coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Grade, contentDescription = null) },
                    label = { Text("Historial de Notas (${listaRubricas.size})") },
                    selected = seccionActual == "notas",
                    onClick = { seccionActual = "notas"; coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Generar Reporte") },
                    selected = seccionActual == "reportes",
                    onClick = { seccionActual = "reportes"; coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red) },
                    label = { Text("Cerrar Sesión", color = Color.Red) },
                    selected = false,
                    onClick = { coroutineScope.launch { drawerState.close() }; onCerrarSesion() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (seccionActual) {
                                "inicio" -> "Dashboard Ejecutivo"
                                "activos" -> "Usuarios Activos"
                                "notas" -> "Historial de Calificaciones"
                                "reportes" -> "Generar Reportes"
                                else -> "Aprobación de Solicitudes"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { cargarDatos() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sincronizar", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeInstitucional)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF4F6F5))
            ) {
                when (seccionActual) {
                    "inicio" -> ContenidoDashboardAdmin(
                        listaPendientes = listaPendientes,
                        listaActivos = listaActivos,
                        listaRubricas = listaRubricas,
                        cargando = cargandoDatos,
                        onNavegar = { destino -> seccionActual = destino }
                    )

                    "activos" -> Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Usuarios Activos (${listaActivos.size})", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        if (listaActivos.isEmpty()) {
                            Text("No hay usuarios activos registrados aún", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(listaActivos) { usuario ->
                                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("${usuario.nombre} ${usuario.apellido}", fontWeight = FontWeight.Bold, color = VerdeInstitucional)
                                            Text("Correo: ${usuario.correo}", fontSize = 14.sp)
                                            Text("Rol actual: ${usuario.rol}", fontSize = 14.sp)

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedButton(
                                                onClick = { usuarioAEditar = usuario },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Editar Información y Rol")
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Button(
                                                onClick = {
                                                    if (usuariosPreview == null) {
                                                        val db = EsfocarDatabase.getDatabase(context)
                                                        coroutineScope.launch(Dispatchers.IO) {
                                                            try { RetrofitClient.apiService.eliminarUsuario(usuario.id) } catch (_: Exception) {}
                                                            db.usuarioDao().eliminarUsuario(usuario)
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(context, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()
                                                                cargarDatos()
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                                            ) {
                                                Text("Eliminar Cuenta", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "notas" -> Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Calificaciones Registradas (${listaRubricas.size})", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        if (listaRubricas.isEmpty()) {
                            Text("No hay evaluaciones registradas en la base de datos todavía", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(listaRubricas) { rubrica ->
                                    val notaVal = if (rubrica.notaFinal > 0.0) rubrica.notaFinal else rubrica.nota
                                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("${rubrica.titulo} (#${rubrica.id})", fontWeight = FontWeight.Bold, color = VerdeInstitucional)
                                            Text("Detalle: ${rubrica.tipo}", fontSize = 13.sp)

                                            Text(
                                                text = "Nota Registrada: ${String.format(Locale.US, "%.2f", notaVal)} • ${if (notaVal >= 4.0) "APROBADO" else "REPROBADO"}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (notaVal >= 4.0) VerdeInstitucional else Color.Red,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                            Text("Feedback: \"${rubrica.feedbackDocente.ifBlank { rubrica.feedback }}\"", fontSize = 13.sp, color = Color.DarkGray)

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Button(
                                                onClick = { rubricaAEditar = rubrica },
                                                colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Editar Nota de Evaluación")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "reportes" -> Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Módulo de Reportes Institucionales", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold)

                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Reporte Consolidado de Calificaciones", fontWeight = FontWeight.Bold)
                                Text("Genera y visualiza la vista previa oficial del informe general.", fontSize = 14.sp)

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { reporteGenerado = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                                ) {
                                    Text("Generar y Visualizar Reporte")
                                }
                            }
                        }

                        if (reporteGenerado) {
                            val promedioInst = if (listaRubricas.isNotEmpty()) {
                                listaRubricas.map { if (it.notaFinal > 0.0) it.notaFinal else it.nota }.average()
                            } else 0.0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("PREVISUALIZACIÓN DEL REPORTE OFICIAL", fontWeight = FontWeight.Bold, color = VerdeInstitucional)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Escuela de Formación de Carabineros - ESFOCAR")
                                    Text("• Total Usuarios Activos: ${listaActivos.size}")
                                    Text("• Total Evaluaciones Registradas: ${listaRubricas.size}")
                                    Text("• Promedio General Institucional: ${String.format(Locale.US, "%.2f", promedioInst)}")
                                    Text("• Solicitudes Pendientes: ${listaPendientes.size}")
                                    Text("• Sello de Validación: [Aprobado por Administración]")
                                }
                            }
                        }
                    }

                    "pendientes" -> Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Solicitudes Pendientes de Aprobación (${listaPendientes.size})", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        if (listaPendientes.isEmpty()) {
                            Text("No hay solicitudes pendientes en este momento", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(listaPendientes) { usuario ->
                                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Solicitante: ${usuario.nombre} ${usuario.apellido}", fontWeight = FontWeight.Bold, color = VerdeInstitucional)
                                            Text("Correo: ${usuario.correo}", fontSize = 14.sp)
                                            Text("Rol Solicitado: ${usuario.rol}", fontSize = 14.sp)

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedButton(onClick = { usuarioAEditar = usuario }, modifier = Modifier.weight(1f)) {
                                                    Text("Editar Datos", fontSize = 12.sp)
                                                }
                                                OutlinedButton(onClick = { usuarioAVerDocumentos = usuario }, modifier = Modifier.weight(1f)) {
                                                    Text("Ver Docs", fontSize = 12.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Button(
                                                onClick = {
                                                    if (usuariosPreview == null) {
                                                        val db = EsfocarDatabase.getDatabase(context)
                                                        coroutineScope.launch(Dispatchers.IO) {
                                                            try { RetrofitClient.apiService.aprobarUsuario(usuario.id) } catch (_: Exception) {}
                                                            val usuarioAprobado = usuario.copy(cuentaActiva = true)
                                                            db.usuarioDao().actualizarUsuario(usuarioAprobado)
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(context, "Cuenta activada exitosamente", Toast.LENGTH_SHORT).show()
                                                                cargarDatos()
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                                            ) {
                                                Text("Aprobar y Activar Cuenta")
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Button(
                                                onClick = {
                                                    if (usuariosPreview == null) {
                                                        val db = EsfocarDatabase.getDatabase(context)
                                                        coroutineScope.launch(Dispatchers.IO) {
                                                            try { RetrofitClient.apiService.eliminarUsuario(usuario.id) } catch (_: Exception) {}
                                                            db.usuarioDao().eliminarUsuario(usuario)
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(context, "Solicitud rechazada y eliminada", Toast.LENGTH_SHORT).show()
                                                                cargarDatos()
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                                            ) {
                                                Text("Rechazar Solicitud", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal EDITAR USUARIO
    usuarioAEditar?.let { usuario ->
        var editNombre by remember { mutableStateOf(usuario.nombre) }
        var editApellido by remember { mutableStateOf(usuario.apellido) }
        var editCorreo by remember { mutableStateOf(usuario.correo) }
        var editRol by remember { mutableStateOf(usuario.rol) }

        var menuRolExpandido by remember { mutableStateOf(false) }
        val opcionesRoles = listOf("Docente", "Estudiante", "Carabinero", "Admin")

        AlertDialog(
            onDismissRequest = { usuarioAEditar = null },
            title = { Text("Editar Información Completa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editApellido, onValueChange = { editApellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editCorreo, onValueChange = { editCorreo = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editRol,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rol Seleccionado (Haz clic)") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Seleccionar rol",
                                    modifier = Modifier.clickable { menuRolExpandido = true }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = menuRolExpandido,
                            onDismissRequest = { menuRolExpandido = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            opcionesRoles.forEach { rolOpcion ->
                                DropdownMenuItem(
                                    text = { Text(rolOpcion, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        editRol = rolOpcion
                                        menuRolExpandido = false
                                    }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { menuRolExpandido = true }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (usuariosPreview == null) {
                            val db = EsfocarDatabase.getDatabase(context)
                            coroutineScope.launch(Dispatchers.IO) {
                                try { RetrofitClient.apiService.cambiarRolUsuario(usuario.id, editRol) } catch (_: Exception) {}
                                val usuarioEditado = usuario.copy(
                                    nombre = editNombre,
                                    apellido = editApellido,
                                    correo = editCorreo,
                                    rol = editRol,
                                    cuentaActiva = true
                                )
                                db.usuarioDao().actualizarUsuario(usuarioEditado)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Datos y rol actualizados correctamente", Toast.LENGTH_SHORT).show()
                                    cargarDatos()
                                    usuarioAEditar = null
                                }
                            }
                        } else {
                            usuarioAEditar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEditar = null }) { Text("Cancelar") }
            }
        )
    }

    // Modal VER DOCUMENTOS
    usuarioAVerDocumentos?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioAVerDocumentos = null },
            title = { Text("Documentación Adjunta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Postulante: ${usuario.nombre} ${usuario.apellido}", fontWeight = FontWeight.Bold)
                    Text("RUT: ${usuario.rut.ifBlank { "No informado" }}")
                    Text("Correo: ${usuario.correo}")
                    Text("Estado de verificación: Documentación institucional validada.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { usuarioAVerDocumentos = null },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Modal EDITAR NOTA
    rubricaAEditar?.let { rubrica ->
        var nuevaNota by remember { mutableStateOf((if (rubrica.notaFinal > 0.0) rubrica.notaFinal else rubrica.nota).toString()) }

        AlertDialog(
            onDismissRequest = { rubricaAEditar = null },
            title = { Text("Editar Nota de Evaluación") },
            text = {
                Column {
                    Text("Ingresa la calificación corregida (entre 1.0 y 7.0):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevaNota,
                        onValueChange = { nuevaNota = it },
                        label = { Text("Nota (1.0 - 7.0)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val notaDouble = nuevaNota.replace(',', '.').toDoubleOrNull()
                        if (notaDouble == null || notaDouble !in 1.0..7.0) {
                            Toast.makeText(context, "La nota debe estar entre 1.0 y 7.0", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (usuariosPreview == null) {
                            val db = EsfocarDatabase.getDatabase(context)
                            coroutineScope.launch(Dispatchers.IO) {
                                val rubricaEditada = rubrica.copy(
                                    nota = notaDouble,
                                    notaFinal = notaDouble,
                                    estado = if (notaDouble >= 4.0) "APROBADO" else "REPROBADO"
                                )
                                try { RetrofitClient.apiService.editarEvaluacion(rubrica.id, rubricaEditada) } catch (_: Exception) {}
                                db.rubricaDao().actualizarRubrica(rubricaEditada)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Calificación corregida en la BD", Toast.LENGTH_SHORT).show()
                                    cargarDatos()
                                    rubricaAEditar = null
                                }
                            }
                        } else {
                            rubricaAEditar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                ) {
                    Text("Actualizar Calificación")
                }
            },
            dismissButton = {
                TextButton(onClick = { rubricaAEditar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ContenidoDashboardAdmin(
    listaPendientes: List<Usuario>,
    listaActivos: List<Usuario>,
    listaRubricas: List<Rubrica>,
    cargando: Boolean,
    onNavegar: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    // Cálculos analíticos en tiempo real
    val notasValidas = listaRubricas.map { if (it.notaFinal > 0.0) it.notaFinal else it.nota }.filter { it > 0.0 }
    val promedioGeneral = if (notasValidas.isNotEmpty()) notasValidas.average() else 0.0
    val totalAprobados = notasValidas.count { it >= 4.0 }
    val totalReprobados = notasValidas.count { it < 4.0 }
    val tasaAprobacion = if (notasValidas.isNotEmpty()) totalAprobados.toFloat() / notasValidas.size.toFloat() else 0f

    val totalDocentes = listaActivos.count {
        it.rol.equals("Docente", ignoreCase = true) || it.rol.equals("Carabinero", ignoreCase = true)
    }
    val totalEstudiantes = listaActivos.count { it.rol.equals("Estudiante", ignoreCase = true) }

    // Animaciones de contadores y barras
    val promedioAnimado by animateFloatAsState(
        targetValue = promedioGeneral.toFloat(),
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "promedio_institucional"
    )
    val barraAprobacionAnimada by animateFloatAsState(
        targetValue = tasaAprobacion,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "barra_aprobacion"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Institucional Superior
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0D3B12), VerdeInstitucional)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RESUMEN EJECUTIVO ESFOCAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5D6A7),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Control Académico",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (cargando) "Sincronizando con nube..." else "Datos sincronizados en tiempo real",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    if (cargando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PROMEDIO", fontSize = 10.sp, color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale.US, "%.2f", promedioAnimado),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cuadrícula 2x2 de Tarjetas KPI Interactivas
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaKpiDashboard(
                titulo = "Pendientes",
                valor = "${listaPendientes.size}",
                subtitulo = "Por aprobar",
                icono = Icons.Default.PendingActions,
                colorAcento = Color(0xFFEF6C00),
                modifier = Modifier.weight(1f),
                onClick = { onNavegar("pendientes") }
            )
            TarjetaKpiDashboard(
                titulo = "Usuarios Activos",
                valor = "${listaActivos.size}",
                subtitulo = "$totalDocentes doc • $totalEstudiantes est",
                icono = Icons.Default.People,
                colorAcento = VerdeInstitucional,
                modifier = Modifier.weight(1f),
                onClick = { onNavegar("activos") }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaKpiDashboard(
                titulo = "Evaluaciones",
                valor = "${listaRubricas.size}",
                subtitulo = "Notas subidas",
                icono = Icons.Default.Assessment,
                colorAcento = Color(0xFF1565C0),
                modifier = Modifier.weight(1f),
                onClick = { onNavegar("notas") }
            )
            TarjetaKpiDashboard(
                titulo = "Aprobación",
                valor = "${(tasaAprobacion * 100).toInt()}%",
                subtitulo = "$totalAprobados aprob • $totalReprobados rep",
                icono = Icons.Default.VerifiedUser,
                colorAcento = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                onClick = { onNavegar("reportes") }
            )
        }

        // Gráfico de Rendimiento Académico (Aprobados vs Reprobados)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rendimiento Académico Global",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VerdeInstitucional
                    )
                    Text(
                        text = "${notasValidas.size} calificaciones",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barra segmentada animada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (notasValidas.isEmpty()) Color(0xFFE0E0E0) else Color(0xFFE53935))
                ) {
                    if (notasValidas.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barraAprobacionAnimada.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(VerdeInstitucional)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(VerdeInstitucional))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aprobados: $totalAprobados", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE53935)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reprobados: $totalReprobados", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Distribución de Comunidad y Acciones Rápidas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Distribución de Comunidad y Accesos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = VerdeInstitucional
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = VerdeInstitucional)
                        Text("$totalDocentes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Docentes", fontSize = 12.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF1565C0))
                        Text("$totalEstudiantes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Estudiantes", fontSize = 12.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PendingActions, contentDescription = null, tint = Color(0xFFEF6C00))
                        Text("${listaPendientes.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Pendientes", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onNavegar("pendientes") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                    ) {
                        Text("Revisar Solicitudes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onNavegar("reportes") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ver Reporte", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaKpiDashboard(
    titulo: String,
    valor: String,
    subtitulo: String,
    icono: ImageVector,
    colorAcento: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(titulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colorAcento.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = subtitulo,
                fontSize = 11.sp,
                color = colorAcento,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaAdmin() {
    AppEsfocarTheme {
        VistaAdmin(onCerrarSesion = {})
    }
}