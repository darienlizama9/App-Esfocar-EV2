package com.example.appesfocar.ui.vistas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaEstudiante(
    rutEstudiante: String = "",
    correoEstudiante: String = "",
    onCerrarSesion: () -> Unit = {}
) {
    var pestañaSeleccionada by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portal Estudiante ESFOCAR", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Grade, contentDescription = "Mis Notas") },
                    label = { Text("Mis Notas") },
                    selected = pestañaSeleccionada == 0,
                    onClick = { pestañaSeleccionada = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Mi Perfil") },
                    label = { Text("Mi Perfil") },
                    selected = pestañaSeleccionada == 1,
                    onClick = { pestañaSeleccionada = 1 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (pestañaSeleccionada) {
                0 -> VistaNotasEstudiante(rutEstudiante = rutEstudiante)
                1 -> VistaPerfilEstudiante(
                    correoEstudiante = correoEstudiante,
                    rutEstudiante = rutEstudiante,
                    onCerrarSesion = onCerrarSesion
                )
            }
        }
    }
}
