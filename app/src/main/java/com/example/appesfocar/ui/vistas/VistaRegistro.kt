package com.example.appesfocar.ui.vistas

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.R
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Usuario
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.theme.VerdeInstitucional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Composable
fun VistaRegistro(
    onRegistroExitoso: () -> Unit,
    onVolverLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { EsfocarDatabase.getDatabase(context) }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    // Configuración del Calendario Nativo
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            fechaNacimiento = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR) - 25,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Función auxiliar para validar formato de correo electrónico
    fun esCorreoValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_esfocar),
            contentDescription = "Logo oficial ESFOCAR",
            modifier = Modifier.fillMaxWidth(0.8f),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Registro de Instructor",
            style = MaterialTheme.typography.headlineSmall,
            color = VerdeInstitucional,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de Fecha con Calendario Nativo
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de Nacimiento (DD/MM/AAAA)") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha",
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { datePickerDialog.show() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo (ej: usuario@gmail.com)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña (mín. 6 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar/Ocultar contraseña")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // 1. Validar campos vacíos
                if (nombre.isBlank() || apellido.isBlank() || fechaNacimiento.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // 2. Validar formato de correo real
                if (!esCorreoValido(correo.trim())) {
                    Toast.makeText(context, "Por favor ingresa un correo electrónico válido", Toast.LENGTH_LONG).show()
                    return@Button
                }

                // 3. Validar contraseña segura
                if (contrasena.length < 6) {
                    Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
                    return@Button
                }

                cargando = true
                coroutineScope.launch(Dispatchers.IO) {
                    // Validar si el correo ya está registrado en la BD
                    val usuarioExistente = db.usuarioDao().obtenerUsuarioPorCorreo(correo.trim())
                    if (usuarioExistente != null) {
                        withContext(Dispatchers.Main) {
                            cargando = false
                            Toast.makeText(context, "Este correo ya está registrado en el sistema", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }

                    val nuevoUsuario = Usuario(
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        fechaNacimiento = fechaNacimiento.trim(),
                        correo = correo.trim(),
                        contrasena = contrasena,
                        rol = "Docente",
                        cuentaActiva = false
                    )
                    db.usuarioDao().registrarUsuario(nuevoUsuario)
                    withContext(Dispatchers.Main) {
                        cargando = false
                        Toast.makeText(
                            context,
                            "Solicitud de registro con éxito. Contacte a su administrador para autorizar permisos.",
                            Toast.LENGTH_LONG
                        ).show()
                        onRegistroExitoso()
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("REGISTRARSE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onVolverLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión aquí", color = VerdeInstitucional)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRegistro() {
    AppEsfocarTheme {
        VistaRegistro(onRegistroExitoso = {}, onVolverLogin = {})
    }
}