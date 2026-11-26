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
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.util.normalizarRut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VistaPerfilEstudiante(
    correoEstudiante: String,
    rutEstudiante: String,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { EsfocarDatabase.getDatabase(context) }

    var nombreCompleto by remember { mutableStateOf("Cargando...") }
    var rutUsuario by remember { mutableStateOf(normalizarRut(rutEstudiante)) }

    LaunchedEffect(correoEstudiante) {
        if (correoEstudiante.isNotBlank()) {
            withContext(Dispatchers.IO) {
                val usuario = db.usuarioDao().obtenerUsuarioPorCorreo(correoEstudiante.trim())
                if (usuario != null) {
                    withContext(Dispatchers.Main) {
                        nombreCompleto = "${usuario.nombre} ${usuario.apellido}"
                        if (usuario.rut.isNotBlank()) {
                            rutUsuario = normalizarRut(usuario.rut)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Perfil Estudiante",
                modifier = Modifier.size(90.dp),
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Información del Estudiante", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))
                    HorizontalDivider()

                    ItemPerfilEstudiante("Nombre:", nombreCompleto)
                    ItemPerfilEstudiante("RUT:", rutUsuario.ifBlank { "Sin RUT" })
                    ItemPerfilEstudiante("Correo Institucional:", correoEstudiante)
                    ItemPerfilEstudiante("Rol:", "Estudiante")
                }
            }
        }

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ItemPerfilEstudiante(titulo: String, valor: String) {
    Column {
        Text(text = titulo, fontSize = 12.sp, color = Color.Gray)
        Text(text = valor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
