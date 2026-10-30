package com.example.appesfocar.ui.vistas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appesfocar.data.EsfocarDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SesionUsuario {
    var correoActual: String = ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionPrincipal() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val mostrarBarraInferior = rutaActual in listOf("ruta_rubricas", "ruta_documentos") || rutaActual?.startsWith("ruta_perfil") == true

    Scaffold(
        bottomBar = {
            if (mostrarBarraInferior) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Rúbricas") },
                        label = { Text("Rúbricas") },
                        selected = rutaActual == "ruta_rubricas",
                        onClick = { navController.navigate("ruta_rubricas") { launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Description, contentDescription = "Documentos") },
                        label = { Text("Documentos") },
                        selected = rutaActual == "ruta_documentos",
                        onClick = { navController.navigate("ruta_documentos") { launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") },
                        selected = rutaActual?.startsWith("ruta_perfil") == true,
                        onClick = {
                            val correoDestino = SesionUsuario.correoActual
                            navController.navigate("ruta_perfil/$correoDestino") { launchSingleTop = true }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        val modifierNavHost = if (mostrarBarraInferior) Modifier.padding(paddingValues) else Modifier.fillMaxSize()

        NavHost(
            navController = navController,
            startDestination = "bienvenida",
            modifier = modifierNavHost
        ) {
            // Flujo de Acceso
            composable("bienvenida") {
                VistaBienvenida(
                    onIniciarSesion = { navController.navigate("login") },
                    onRegistrarse = { navController.navigate("registro") }
                )
            }

            composable("login") {
                VistaLogin(
                    onLoginExitoso = { rol, correoUsuario ->
                        // Guardamos el correo en el objeto global al iniciar sesión
                        SesionUsuario.correoActual = correoUsuario

                        if (rol.equals("Admin", ignoreCase = true) || rol.equals("Administrador", ignoreCase = true)) {
                            navController.navigate("admin") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("ruta_perfil/$correoUsuario") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    onRegistrar = { navController.navigate("registro") }
                )
            }

            composable("registro") {
                VistaRegistro(
                    onRegistroExitoso = {
                        navController.navigate("login") {
                            popUpTo("registro") { inclusive = true }
                        }
                    },
                    onVolverLogin = { navController.popBackStack() }
                )
            }

            composable("admin") {
                VistaAdmin(
                    onCerrarSesion = {
                        SesionUsuario.correoActual = "" // Limpiamos la sesión al salir
                        navController.navigate("bienvenida") {
                            popUpTo(0)
                        }
                    }
                )
            }

            // Flujo Interno del Docente
            composable("ruta_rubricas") { VistaRubricas() }
            composable("ruta_documentos") { VistaDocumentos() }

            composable("ruta_perfil/{correoUsuario}") { backStackEntry ->
                val correoParam = backStackEntry.arguments?.getString("correoUsuario")
                val correoFinal = if (!correoParam.isNullOrBlank()) correoParam else SesionUsuario.correoActual
                VistaPerfil(navController, correoFinal)
            }

            composable("ruta_perfil/") {
                VistaPerfil(navController, correo = SesionUsuario.correoActual)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaPerfil(navController: NavHostController, correo: String) {
    val context = LocalContext.current
    val db = remember { EsfocarDatabase.getDatabase(context) }

    var nombre by remember { mutableStateOf("Cargando...") }
    var apellido by remember { mutableStateOf("") }
    var correoInstitucional by remember { mutableStateOf(correo) }
    var rolUsuario by remember { mutableStateOf("Docente") }
    var estadoCuenta by remember { mutableStateOf("Autorizada") }

    LaunchedEffect(correo) {
        val correoAUsar = if (correo.isNotBlank()) correo else SesionUsuario.correoActual
        if (correoAUsar.isNotBlank()) {
            withContext(Dispatchers.IO) {
                val usuarioEncontrado = db.usuarioDao().obtenerUsuarioPorCorreo(correoAUsar)
                if (usuarioEncontrado != null) {
                    withContext(Dispatchers.Main) {
                        nombre = usuarioEncontrado.nombre
                        apellido = usuarioEncontrado.apellido
                        correoInstitucional = usuarioEncontrado.correo
                        rolUsuario = usuarioEncontrado.rol
                        estadoCuenta = if (usuarioEncontrado.cuentaActiva) "Autorizada" else "Pendiente"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFF1B5E20)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Datos de la Cuenta", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))
                        HorizontalDivider()

                        DatoPerfil(titulo = "Nombre completo:", valor = "$nombre $apellido")
                        DatoPerfil(titulo = "Correo:", valor = correoInstitucional)
                        DatoPerfil(titulo = "Rol asignado:", valor = rolUsuario)
                        DatoPerfil(titulo = "Estado de cuenta:", valor = estadoCuenta)
                    }
                }
            }

            Button(
                onClick = {
                    SesionUsuario.correoActual = "" // Limpiar sesión al cerrar
                    navController.navigate("bienvenida") {
                        popUpTo(0)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) {
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun DatoPerfil(titulo: String, valor: String) {
    Column {
        Text(text = titulo, fontSize = 12.sp, color = Color.Gray)
        Text(text = valor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewNavegacionPrincipal() {
    NavegacionPrincipal()
}