package com.example.appesfocar.ui.vistas

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
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

    // Control del Menú Lateral (Drawer)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Estado para controlar en qué pantalla/sección estamos
    var seccionActual by remember { mutableStateOf("inicio") }

    var listaPendientes by remember { mutableStateOf(usuariosPreview ?: emptyList()) }

    // Diálogos emergentes
    var usuarioAEditar by remember { mutableStateOf<Usuario?>(null) }
    var usuarioAVerDocumentos by remember { mutableStateOf<Usuario?>(null) }

    if (usuariosPreview == null) {
        val db = remember { EsfocarDatabase.getDatabase(context) }

        fun cargarPendientes() {
            coroutineScope.launch(Dispatchers.IO) {
                val pendientes = db.usuarioDao().obtenerUsuariosPendientes()
                withContext(Dispatchers.Main) {
                    listaPendientes = pendientes
                }
            }
        }

        LaunchedEffect(Unit) {
            cargarPendientes()
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
                    Text(
                        text = "Panel de Administración",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ESFOCAR - Sistema Evaluativo",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opción 1: Inicio
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") },
                    selected = seccionActual == "inicio",
                    onClick = {
                        seccionActual = "inicio"
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Opción 2: Aprobar Docentes
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("Aprobar Docentes (${listaPendientes.size})") },
                    selected = seccionActual == "pendientes",
                    onClick = {
                        seccionActual = "pendientes"
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Opción 3: Generar Reporte
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Generar Reporte") },
                    selected = seccionActual == "reportes",
                    onClick = {
                        seccionActual = "reportes"
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Opción 4: Cerrar Sesión
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red) },
                    label = { Text("Cerrar Sesión", color = Color.Red) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onCerrarSesion()
                    },
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
                                "inicio" -> "Inicio Administrador"
                                "reportes" -> "Generar Reportes"
                                else -> "Aprobación de Docentes"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = VerdeInstitucional
                    )
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
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Bienvenido/a al Panel de Administración",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VerdeInstitucional
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Usa el menú lateral para navegar por las opciones del sistema.",
                                color = Color.Gray
                            )
                        }
                    }

                    "reportes" -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = "Módulo de Generación de Reportes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VerdeInstitucional,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Reporte General de Docentes", fontWeight = FontWeight.Bold)
                                    Text("Genera un resumen en PDF con los usuarios registrados y su estado actual.", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Generando reporte...", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                                    ) {
                                        Text("Descargar Reporte PDF")
                                    }
                                }
                            }
                        }
                    }

                    "pendientes" -> {
                        Text(
                            text = "Docentes Pendientes de Aprobación (${listaPendientes.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = VerdeInstitucional,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (listaPendientes.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("No hay solicitudes pendientes en este momento", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(listaPendientes) { usuario ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = "Instructor: ${usuario.nombre} ${usuario.apellido}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = VerdeInstitucional
                                            )
                                            Text("Fecha Nacimiento: ${usuario.fechaNacimiento}", fontSize = 14.sp)
                                            Text("Correo Institucional: ${usuario.correo}", fontSize = 14.sp)
                                            Text("Rol Solicitado: ${usuario.rol}", fontSize = 14.sp)

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { usuarioAEditar = usuario },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Editar Datos", fontSize = 12.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { usuarioAVerDocumentos = usuario },
                                                    modifier = Modifier.weight(1f)
                                                ) {
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

        AlertDialog(
            onDismissRequest = { usuarioAEditar = null },
            title = { Text("Editar Datos de Instructor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre") }
                    )
                    OutlinedTextField(
                        value = editApellido,
                        onValueChange = { editApellido = it },
                        label = { Text("Apellido") }
                    )
                    OutlinedTextField(
                        value = editCorreo,
                        onValueChange = { editCorreo = it },
                        label = { Text("Correo") }
                    )
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
                                    correo = editCorreo
                                )
                                db.usuarioDao().actualizarUsuario(usuarioEditado)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                                    listaPendientes = db.usuarioDao().obtenerUsuariosPendientes()
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
                TextButton(onClick = { usuarioAEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para VER DOCUMENTOS
    usuarioAVerDocumentos?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioAVerDocumentos = null },
            title = { Text("Documentos Adjuntos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Documentos de: ${usuario.nombre} ${usuario.apellido}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Título Profesional / Grado Académico: [Cargado ✓]")
                    Text("• Certificado de Antecedentes: [Cargado ✓]")
                    Text("• Curriculum Vitae Institucional: [Cargado ✓]")
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaAdmin() {
    val listaPrueba = listOf(
        Usuario(
            id = 1,
            nombre = "Carlos",
            apellido = "Pérez",
            fechaNacimiento = "15/08/1988",
            correo = "carlos.perez@carabineros.cl",
            contrasena = "123456",
            rol = "Docente",
            cuentaActiva = false
        )
    )

    AppEsfocarTheme {
        VistaAdmin(
            onCerrarSesion = {},
            usuariosPreview = listaPrueba
        )
    }
}