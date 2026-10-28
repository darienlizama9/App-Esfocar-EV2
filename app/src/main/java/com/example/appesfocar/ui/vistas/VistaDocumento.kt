package com.example.appesfocar.ui.vistas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Dato temporal simulado para probar el diseño antes de conectar el explorador real
data class DocumentoSimulado(val id: Int, val nombre: String)

@Composable
fun VistaDocumentos() {
    val documentosFalsos = listOf(
        DocumentoSimulado(1, "Reglamento_ESFOCAR.pdf"),
        DocumentoSimulado(2, "Pauta_Evaluacion.docx")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { /* En el próximo paso abriremos los archivos aquí */ },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
        ) {
            Text("Cargar nuevo documento")
        }

        Text(
            text = "Mis Documentos",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(documentosFalsos) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = doc.nombre, modifier = Modifier.weight(1f))
                        Row {
                            IconButton(onClick = { /* Aquí irá el Intent nativo */ }) {
                                Icon(Icons.Default.Visibility, contentDescription = "Ver documento")
                            }
                            IconButton(onClick = { /* Acción para eliminar */ }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}