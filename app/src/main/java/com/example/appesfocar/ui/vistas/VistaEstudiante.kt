package com.example.appesfocar.ui.vistas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.theme.VerdeInstitucional

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaEstudiante(
    rutEstudiante: String = "",
    correoEstudiante: String = "",
    onCerrarSesion: () -> Unit = {}
) {
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFA5D6A7),
                                modifier = Modifier.padding(6.dp).size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Portal Estudiante ESFOCAR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = if (pestanaSeleccionada == 0) "Calificaciones y Rúbricas" else "Credencial y Datos Personales",
                                color = Color(0xFFA5D6A7),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdeInstitucional)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Grade, contentDescription = "Mis Notas") },
                    label = { Text("Mis Notas", fontWeight = FontWeight.SemiBold) },
                    selected = pestanaSeleccionada == 0,
                    onClick = { pestanaSeleccionada = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VerdeInstitucional,
                        selectedTextColor = VerdeInstitucional,
                        indicatorColor = VerdeInstitucional.copy(alpha = 0.14f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Mi Perfil") },
                    label = { Text("Mi Perfil", fontWeight = FontWeight.SemiBold) },
                    selected = pestanaSeleccionada == 1,
                    onClick = { pestanaSeleccionada = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VerdeInstitucional,
                        selectedTextColor = VerdeInstitucional,
                        indicatorColor = VerdeInstitucional.copy(alpha = 0.14f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF4F6F5))
        ) {
            when (pestanaSeleccionada) {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaEstudiante() {
    AppEsfocarTheme {
        VistaEstudiante(
            rutEstudiante = "12345678-9",
            correoEstudiante = "estudiante@esfocar.cl"
        )
    }
}