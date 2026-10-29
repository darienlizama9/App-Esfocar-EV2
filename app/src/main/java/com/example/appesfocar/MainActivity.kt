package com.example.appesfocar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.appesfocar.ui.theme.AppEsfocarTheme
import com.example.appesfocar.ui.vistas.NavegacionPrincipal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppEsfocarTheme {
                NavegacionPrincipal()
            }
        }
    }
}