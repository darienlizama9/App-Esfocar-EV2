package com.example.appesfocar.repository

sealed class ResultadoSync<out T> {
    data class ExitoServidor<T>(val data: T, val mensaje: String) : ResultadoSync<T>()
    data class GuardadoLocalOffline<T>(val data: T, val mensaje: String) : ResultadoSync<T>()
    data class Error<T>(val mensaje: String, val dataLocal: T? = null) : ResultadoSync<T>()
}
