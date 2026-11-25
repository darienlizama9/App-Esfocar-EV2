package com.example.appesfocar.ui.vistas

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.R
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Usuario
import com.example.appesfocar.repository.EsfocarRepository
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.theme.VerdeInstitucional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaRegistro(
    onRegistroExitoso: () -> Unit,
    onVolverLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val db = remember { EsfocarDatabase.getDatabase(context) }
    val repository = remember { EsfocarRepository(db) }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf("Docente") }
    var rut by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    fun esCorreoValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun esRutValido(rutInput: String): Boolean {
        val regex = "^[0-9]{7,8}-[0-9kK]$".toRegex()
        return regex.matches(rutInput)
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        fechaNacimiento = formatter.format(Date(millis))
                    }
                    mostrarDatePicker = false
                }) {
                    Text("ACEPTAR", color = VerdeInstitucional, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_esfocar),
            contentDescription = "Logo oficial ESFOCAR",
            modifier = Modifier.fillMaxWidth(0.7f),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Registro de Usuario",
            style = MaterialTheme.typography.headlineSmall,
            color = VerdeInstitucional,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Seleccione su perfil:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = rolSeleccionado == "Docente",
                onClick = {
                    rolSeleccionado = "Docente"
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                label = { Text("Docente") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeInstitucional,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = rolSeleccionado == "Estudiante",
                onClick = {
                    rolSeleccionado = "Estudiante"
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                label = { Text("Estudiante") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeInstitucional,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = if (rolSeleccionado == "Estudiante") ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(visible = rolSeleccionado == "Estudiante") {
            OutlinedTextField(
                value = rut,
                onValueChange = { rut = it },
                label = { Text("RUT (Ej: 12345678-9)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de Nacimiento (Toca para seleccionar)") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha",
                        modifier = Modifier.clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            mostrarDatePicker = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        mostrarDatePicker = true
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo (ej: usuario@esfocar.cl)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña (mín. 6 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ),
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
                focusManager.clearFocus()
                keyboardController?.hide()

                if (nombre.isBlank() || apellido.isBlank() || fechaNacimiento.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (rolSeleccionado == "Estudiante" && !esRutValido(rut.trim())) {
                    Toast.makeText(context, "El RUT ingresado no es válido (Ej: 12345678-9)", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (!esCorreoValido(correo.trim())) {
                    Toast.makeText(context, "Por favor ingresa un correo electrónico válido", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (contrasena.length < 6) {
                    Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
                    return@Button
                }

                cargando = true
                coroutineScope.launch(Dispatchers.IO) {
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
                        rol = rolSeleccionado,
                        cuentaActiva = false,
                        rut = rut.trim()
                    )
                    repository.registro(nuevoUsuario)
                    withContext(Dispatchers.Main) {
                        cargando = false
                        Toast.makeText(
                            context,
                            "Solicitud enviada con éxito. Un administrador activará su cuenta.",
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

        TextButton(onClick = {
            focusManager.clearFocus()
            keyboardController?.hide()
            onVolverLogin()
        }) {
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