package com.example.appesfocar.ui.vistas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.data.Documento
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.repository.EsfocarRepository
import com.example.appesfocar.repository.ResultadoSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun obtenerNombreRealArchivo(context: Context, uri: Uri): String {
    var nombre = ""
    if (uri.scheme == "content") {
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        nombre = it.getString(index)
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
    if (nombre.isBlank()) {
        nombre = uri.lastPathSegment?.substringAfterLast('/') ?: "Documento_Adjunto.pdf"
    }
    return nombre
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaDocumentos() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { EsfocarDatabase.getDatabase(context) }
    val repository = remember { EsfocarRepository(db) }

    var documentosGuardados by remember { mutableStateOf(listOf<Documento>()) }
    var documentoAEditar by remember { mutableStateOf<Documento?>(null) }
    var nuevoNombreDoc by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    fun cargarDocumentos() {
        coroutineScope.launch(Dispatchers.IO) {
            val docs = repository.listarDocumentos()
            withContext(Dispatchers.Main) {
                documentosGuardados = docs
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDocumentos()
    }

    val selectorArchivos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            val nombreReal = obtenerNombreRealArchivo(context, uri)
            val nuevoDoc = Documento(
                nombreArchivo = nombreReal,
                rutaUri = uri.toString(),
                tipoArchivo = if (nombreReal.endsWith(".pdf", ignoreCase = true)) "PDF" else "Documento"
            )

            coroutineScope.launch(Dispatchers.IO) {
                val resultadoSync = repository.guardarDocumento(nuevoDoc)
                val docsActualizados = repository.listarDocumentos()
                withContext(Dispatchers.Main) {
                    documentosGuardados = docsActualizados
                    val mensaje = when (resultadoSync) {
                        is ResultadoSync.ExitoServidor -> resultadoSync.mensaje
                        is ResultadoSync.GuardadoLocalOffline -> resultadoSync.mensaje
                        is ResultadoSync.Error -> resultadoSync.mensaje
                    }
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Documentos", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Cargar",
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Repositorio Digital ESFOCAR",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sube pautas, reglamentos o manuales (PDF / Word)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectorArchivos.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "text/plain"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Text("SELECCIONAR ARCHIVO", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Documentos Almacenados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = "(${documentosGuardados.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1B5E20))
                }
            } else if (documentosGuardados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay documentos cargados.\nSube uno para comenzar.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documentosGuardados) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.nombreArchivo,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = doc.tipoArchivo.ifBlank { "Archivo adjunto" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        if (doc.rutaUri.isNotBlank()) {
                                            val uri = Uri.parse(doc.rutaUri)
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, "application/*")
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                Toast.makeText(context, "No hay aplicación para abrir este archivo", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = "Ver documento",
                                            tint = Color(0xFF1B5E20)
                                        )
                                    }

                                    IconButton(onClick = {
                                        documentoAEditar = doc
                                        nuevoNombreDoc = doc.nombreArchivo
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = Color(0xFF1976D2)
                                        )
                                    }

                                    IconButton(onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            repository.eliminarDocumento(doc)
                                            val docsActualizados = repository.listarDocumentos()
                                            withContext(Dispatchers.Main) {
                                                documentosGuardados = docsActualizados
                                                Toast.makeText(context, "Documento eliminado", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para editar nombre del documento
    documentoAEditar?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentoAEditar = null },
            title = { Text("Editar Documento") },
            text = {
                OutlinedTextField(
                    value = nuevoNombreDoc,
                    onValueChange = { nuevoNombreDoc = it },
                    label = { Text("Nombre del archivo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoNombreDoc.isNotBlank()) {
                            val docModificado = doc.copy(nombreArchivo = nuevoNombreDoc.trim())
                            coroutineScope.launch(Dispatchers.IO) {
                                val resSync = repository.editarDocumento(docModificado)
                                val docsActualizados = repository.listarDocumentos()
                                withContext(Dispatchers.Main) {
                                    documentosGuardados = docsActualizados
                                    val mensaje = when (resSync) {
                                        is ResultadoSync.ExitoServidor -> resSync.mensaje
                                        is ResultadoSync.GuardadoLocalOffline -> resSync.mensaje
                                        is ResultadoSync.Error -> resSync.mensaje
                                    }
                                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                    documentoAEditar = null
                                }
                            }
                        } else {
                            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { documentoAEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVistaDocumentos() {
    VistaDocumentos()
}
