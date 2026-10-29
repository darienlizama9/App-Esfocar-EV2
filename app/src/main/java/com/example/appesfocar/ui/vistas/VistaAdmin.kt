package com.example.appesfocar.ui.vistas

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.data.Usuario
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

    var usuarioAEditar by remember { mutableStateOf<Usuario?>(null) }
    var usuarioAVerDocumentos by remember { mutableStateOf<Usuario?>(null) }
    var rubricaAEditar by remember { mutableStateOf<Rubrica?>(null) }
    var reporteGenerado by remember { mutableStateOf(false) }

    if (usuariosPreview == null) {
        val db = remember { EsfocarDatabase.getDatabase(context) }

        fun cargarDatos() {
            coroutineScope.launch(Dispatchers.IO) {
                val pendientes = db.usuarioDao().obtenerUsuariosPendientes()
                val todos = db.usuarioDao().obtenerTodosLosUsuarios()
                val rubricas = db.rubricaDao().obtenerTodasLasRubricas()
                withContext(Dispatchers.Main) {
                    listaPendientes = pendientes
                    listaActivos = todos.filter { it.cuentaActiva }
                    listaRubricas = rubricas
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarDatos()
        }
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
                    label = { Text("Docentes Registrados (${listaActivos.size})") },
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
                                "inicio" -> "Dashboard Administrador"
                                "activos" -> "Docentes y Carabineros Activos"
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeInstitucional)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                when (seccionActual) {
                    "inicio" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Resumen General del Sistema", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold)

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier.weight(1f).clickable { seccionActual = "pendientes" },
                                    colors = CardDefaults.cardColors(containerColor = VerdeInstitucional.copy(alpha = 0.1f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerdeInstitucional, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Pendientes", fontWeight = FontWeight.Bold)
                                        Text("${listaPendientes.size} solicitudes", fontSize = 16.sp, color = VerdeInstitucional, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f).clickable { seccionActual = "activos" },
                                    colors = CardDefaults.cardColors(containerColor = VerdeInstitucional.copy(alpha = 0.1f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Icon(Icons.Default.People, contentDescription = null, tint = VerdeInstitucional, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Docentes Activos", fontWeight = FontWeight.Bold)
                                        Text("${listaActivos.size} usuarios", fontSize = 16.sp, color = VerdeInstitucional, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    "activos" -> {
                        Text("Docentes y Carabineros Activos (${listaActivos.size})", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        if (listaActivos.isEmpty()) {
                            Text("No hay usuarios activos registrados aún", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(listaActivos) { usuario ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
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
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "notas" -> {
                        Text("Calificaciones Guardadas en Room (${listaRubricas.size})", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        if (listaRubricas.isEmpty()) {
                            Text("No hay evaluaciones registradas en la base de datos todavía", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(listaRubricas) { rubrica ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("${rubrica.titulo} (#${rubrica.id})", fontWeight = FontWeight.Bold, color = VerdeInstitucional)
                                            Text("Tipo: ${rubrica.tipo}", fontSize = 13.sp)

                                            Text(
                                                text = "Nota Registrada: ${String.format(Locale.US, "%.2f", rubrica.nota)} - ${if (rubrica.nota >= 4.0) "APROBADO" else "REPROBADO"}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (rubrica.nota >= 4.0) VerdeInstitucional else Color.Red,
                                                fontSize = 15.sp
                                            )
                                            Text("Feedback: \"${rubrica.feedback}\"", fontSize = 13.sp, color = Color.DarkGray)

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

                    "reportes" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Módulo de Reportes PDF", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold)

                            Card(modifier = Modifier.fillMaxWidth()) {
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
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("📄 PREVISUALIZACIÓN DEL REPORTE", fontWeight = FontWeight.Bold, color = VerdeInstitucional)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Escuela de Formación de Carabineros - ESFOCAR")
                                        Text("• Total Docentes Registrados: ${listaActivos.size}")
                                        Text("• Total Evaluaciones Guardadas: ${listaRubricas.size}")
                                        Text("• Solicitudes Pendientes: ${listaPendientes.size}")
                                        Text("• Sello de Validación: [Aprobado por Administración]")
                                    }
                                }
                            }
                        }
                    }

                    "pendientes" -> {
                        Text("Docentes Pendientes de Aprobación (${listaPendientes.size})", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        if (listaPendientes.isEmpty()) {
                            Text("No hay solicitudes pendientes en este momento", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(listaPendientes) { usuario ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Instructor: ${usuario.nombre} ${usuario.apellido}", fontWeight = FontWeight.Bold, color = VerdeInstitucional)
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
                                                            val usuarioAprobado = usuario.copy(cuentaActiva = true)
                                                            db.usuarioDao().actualizarUsuario(usuarioAprobado)
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(context, "Docente activado exitosamente", Toast.LENGTH_SHORT).show()
                                                                listaPendientes = db.usuarioDao().obtenerUsuariosPendientes()
                                                                listaActivos = db.usuarioDao().obtenerTodosLosUsuarios().filter { it.cuentaActiva }
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                                            ) {
                                                Text("Aprobar y Activar Cuenta")
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

    // Modal EDITAR USUARIO CON MENÚ DESPLEGABLE DE ROL
    usuarioAEditar?.let { usuario ->
        var editNombre by remember { mutableStateOf(usuario.nombre) }
        var editApellido by remember { mutableStateOf(usuario.apellido) }
        var editCorreo by remember { mutableStateOf(usuario.correo) }
        var editRol by remember { mutableStateOf(usuario.rol) }

        var menuRolExpandido by remember { mutableStateOf(false) }
        val opcionesRoles = listOf("Docente", "Carabinero", "Admin")

        AlertDialog(
            onDismissRequest = { usuarioAEditar = null },
            title = { Text("Editar Información Completa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editApellido, onValueChange = { editApellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editCorreo, onValueChange = { editCorreo = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())

                    // Selector Desplegable de Rol
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
                                val usuarioEditado = usuario.copy(
                                    nombre = editNombre,
                                    apellido = editApellido,
                                    correo = editCorreo,
                                    rol = editRol
                                )
                                db.usuarioDao().actualizarUsuario(usuarioEditado)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                                    listaPendientes = db.usuarioDao().obtenerUsuariosPendientes()
                                    listaActivos = db.usuarioDao().obtenerTodosLosUsuarios().filter { it.cuentaActiva }
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

    // Modal EDITAR NOTA EN ROOM
    rubricaAEditar?.let { rubrica ->
        var nuevaNota by remember { mutableStateOf(rubrica.nota.toString()) }

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
                                val rubricaEditada = rubrica.copy(nota = notaDouble)
                                db.rubricaDao().actualizarRubrica(rubricaEditada)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Calificación corregida en la BD", Toast.LENGTH_SHORT).show()
                                    listaRubricas = db.rubricaDao().obtenerTodasLasRubricas()
                                    rubricaAEditar = null
                                }
                            }
                        } else {
                            rubricaAEditar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                ) {
                    Text("Actualizar en Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { rubricaAEditar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaAdmin() {
    AppEsfocarTheme {
        VistaAdmin(onCerrarSesion = {})
    }
}