package com.example.appesfocar.ui.vistas
import androidx.compose.ui.res.painterResource
import com.example.appesfocar.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.theme.VerdeInstitucional

@Composable
fun VistaLogin(onIngresar: () -> Unit, onRegistrar: () -> Unit) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

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

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sistema de Evaluación",
            style = MaterialTheme.typography.headlineSmall,
            color = VerdeInstitucional,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Docentes e Instructores", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // Formularios Material 3
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo institucional") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onIngresar,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
        ) {
            Text("INICIAR SESIÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onRegistrar) {
            Text("¿Nuevo instructor? Regístrate aquí", color = VerdeInstitucional)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLogin() {
    AppEsfocarTheme {
        VistaLogin(onIngresar = {}, onRegistrar = {})
    }
}