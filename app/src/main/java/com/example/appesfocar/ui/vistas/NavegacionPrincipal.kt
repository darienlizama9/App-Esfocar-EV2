package com.example.appesfocar.ui.vistas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionPrincipal() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    // Ocultar la barra superior e inferior en pantallas de acceso
    val mostrarBarras = rutaActual in listOf("ruta_rubricas", "ruta_documentos", "ruta_perfil")

    Scaffold(
        topBar = {
            if (mostrarBarras) {
                TopAppBar(
                    title = { Text("Sistema ESFOCAR", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1B5E20)
                    )
                )
            }
        },
        bottomBar = {
            if (mostrarBarras) {
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
                        selected = rutaActual == "ruta_perfil",
                        onClick = { navController.navigate("ruta_perfil") { launchSingleTop = true } }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "bienvenida",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Flujo de Acceso
            composable("bienvenida") {
                VistaBienvenida(
                    onComenzar = { navController.navigate("login") }
                )
            }

            composable("login") {
                VistaLogin(
                    onLoginExitoso = { rol ->
                        if (rol.equals("Admin", ignoreCase = true) || rol.equals("Administrador", ignoreCase = true)) {
                            navController.navigate("admin") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("ruta_rubricas") {
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
                        navController.navigate("login") {
                            popUpTo("admin") { inclusive = true }
                        }
                    }
                )
            }

            // Flujo Interno (Con Barra Inferior)
            composable("ruta_rubricas") { VistaRubricas() }
            composable("ruta_documentos") { VistaDocumento() }
            composable("ruta_perfil") { VistaPerfil() }
        }
    }
}

@Composable
fun VistaPerfil() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Perfil en construcción")
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewNavegacionPrincipal() {
    NavegacionPrincipal()
}