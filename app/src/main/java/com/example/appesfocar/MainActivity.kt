package com.example.appesfocar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.vistas.VistaLogin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppEsfocarTheme {
                // Aquí le decimos a la app que inicie directamente en nuestra VistaLogin
                VistaLogin(
                    onIngresar = {
                        // Más adelante programaremos aquí la conexión a la base de datos
                    },
                    onRegistrar = {
                        // Más adelante haremos que esto te lleve a la pantalla de Registro
                    }
                )
            }
        }
    }
}