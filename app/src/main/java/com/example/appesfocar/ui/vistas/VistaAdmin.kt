package com.example.appesfocar.ui.vistas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.example.appesfocar.data.Usuario
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.theme.VerdeInstitucional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    var usuarioAEditar by remember { mutableStateOf<Usuario?>(null) }
    var usuarioAVerDocumentos by remember { mutableStateOf<Usuario?>(null) }
    var reporteGenerado by remember { mutableStateOf(false) }

    var notaEjemplo by remember { mutableStateOf("2.75") }
    var editarNotaModal by remember { mutableStateOf(false) }

    if (usuariosPreview == null) {
        val db = remember { EsfocarDatabase.getDatabase(context) }

        fun cargarDatos() {
            coroutineScope.launch(Dispatchers.IO) {
                val pendientes = db.usuarioDao().obtenerUsuariosPendientes()
                val todos = db.usuarioDao().obtenerTodosLosUsuarios()
                withContext(Dispatchers.Main) {
                    listaPendientes = pendientes
                    listaActivos = todos.filter { it.cuentaActiva }
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
                    label = { Text("Historial de Notas") },
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
                                "notas" -> "Gestión de Notas y Rúbricas"
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
                                        Text("• Solicitudes Pendientes: ${listaPendientes.size}")
                                        Text("• Estado Base de Datos: Sincronizada OK")
                                        Text("• Sello de Validación: [Aprobado por Administración]")
                                    }
                                }
                            }
                        }
                    }

                    "notas" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Registro y Edición de Rúbricas / Notas", style = MaterialTheme.typography.titleMedium, color = VerdeInstitucional, fontWeight = FontWeight.Bold)

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Evaluación General - Rúbrica Institucional", fontWeight = FontWeight.Bold)
                                    Text("Calificación Actual Registrada: $notaEjemplo", fontSize = 16.sp, color = if ((notaEjemplo.toFloatOrNull() ?: 0f) >= 4.0) VerdeInstitucional else Color.Red, fontWeight = FontWeight.Bold)
                                    Text("Estado: ${if ((notaEjemplo.toFloatOrNull() ?: 0f) >= 4.0) "APROBADO" else "REPROBADO"}")

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { editarNotaModal = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                                    ) {
                                        Text("Editar Nota de Docente")
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

    // Modal para EDITAR DATOS
    usuarioAEditar?.let { usuario ->
        var editNombre by remember { mutableStateOf(usuario.nombre) }
        var editApellido by remember { mutableStateOf(usuario.apellido) }
        var editCorreo by remember { mutableStateOf(usuario.correo) }
        var editRol by remember { mutableStateOf(usuario.rol) }

        AlertDialog(
            onDismissRequest = { usuarioAEditar = null },
            title = { Text("Editar Información Completa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = editApellido, onValueChange = { editApellido = it }, label = { Text("Apellido") })
                    OutlinedTextField(value = editCorreo, onValueChange = { editCorreo = it }, label = { Text("Correo") })
                    OutlinedTextField(value = editRol, onValueChange = { editRol = it }, label = { Text("Rol (Docente / Carabinero / Admin)") })
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

    // Modal para EDITAR NOTA
    if (editarNotaModal) {
        var nuevaNota by remember { mutableStateOf(notaEjemplo) }

        AlertDialog(
            onDismissRequest = { editarNotaModal = false },
            title = { Text("Corregir Nota de Docente") },
            text = {
                Column {
                    Text("Ingresa la calificación corregida (ej: 4.0):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = nuevaNota, onValueChange = { nuevaNota = it }, label = { Text("Nota (1.0 a 7.0)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        notaEjemplo = nuevaNota
                        editarNotaModal = false
                        Toast.makeText(context, "Nota actualizada a $nuevaNota", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                ) {
                    Text("Actualizar Nota")
                }
            },
            dismissButton = {
                TextButton(onClick = { editarNotaModal = false }) { Text("Cancelar") }
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