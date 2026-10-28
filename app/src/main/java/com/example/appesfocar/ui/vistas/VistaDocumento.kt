package com.example.appesfocar.ui.vistas
import androidx.compose.ui.tooling.preview.Preview
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Usamos el mismo modelo temporal mientras Jean conecta la Base de Datos central
data class DocumentoTemporal(val id: Int, val nombre: String, val uri: Uri?)

@Composable
fun VistaDocumentos() {
    val context = LocalContext.current

    // Lista de estado que simula nuestra base de datos por ahora
    var documentosGuardados by remember { mutableStateOf(listOf<DocumentoTemporal>()) }

    // Este es el "Lanzador" que abre el explorador de archivos nativo de Android
    val selectorArchivos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Cuando se selecciona un archivo, le pedimos permiso a Android para mantenerlo
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // Simulación de guardar en Base de Datos: Agregamos a la lista
            val nuevoDoc = DocumentoTemporal(
                id = documentosGuardados.size + 1,
                nombre = "Documento_${documentosGuardados.size + 1}.pdf", // Idealmente aquí sacarías el nombre real
                uri = uri
            )
            documentosGuardados = documentosGuardados + nuevoDoc

            Toast.makeText(context, "Documento cargado con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                // Al hacer clic, abrimos el explorador buscando PDFs o documentos Word
                selectorArchivos.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
        ) {
            Text("Cargar nuevo documento (PDF/Word)")
        }

        Text(
            text = "Mis Documentos",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
        )

        if (documentosGuardados.isEmpty()) {
            Text("No hay documentos cargados. Sube uno para comenzar.", color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(documentosGuardados) { doc ->
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
                                // Botón para Ver el archivo
                                IconButton(onClick = {
                                    doc.uri?.let { uri ->
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No tienes una app para abrir este archivo", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Ver documento")
                                }

                                // Botón para Eliminar
                                IconButton(onClick = {
                                    documentosGuardados = documentosGuardados.filter { it.id != doc.id }
                                    Toast.makeText(context, "Documento eliminado", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaDocumentos() {
    VistaDocumentos()
}