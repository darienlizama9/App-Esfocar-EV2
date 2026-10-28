package com.example.appesfocar.ui.vistas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionPrincipal() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sistema ESFOCAR", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20)
                )
            )
        },
        bottomBar = {
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "ruta_rubricas",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("ruta_rubricas") { VistaRubricas() }
            composable("ruta_documentos") { VistaDocumentos() }
            composable("ruta_perfil") { VistaPerfil() }
        }
    }
}

@Composable
fun VistaRubricas() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Rúbricas en construcción")
    }
}

@Composable
fun VistaDocumentos() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Documentos en construcción")
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