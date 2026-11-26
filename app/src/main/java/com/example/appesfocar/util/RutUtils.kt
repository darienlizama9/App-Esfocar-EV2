package com.example.appesfocar.util

fun normalizarRut(rut: String): String {
    if (rut.isBlank()) return ""
    val limpio = rut.replace(".", "").replace(" ", "").replace("-", "").trim().uppercase()
    if (limpio.isEmpty()) return ""
    return if (limpio.length > 1) {
        val cuerpo = limpio.substring(0, limpio.length - 1)
        val dv = limpio.last()
        "$cuerpo-$dv"
    } else {
        limpio
    }
}

fun esRutValido(rut: String): Boolean {
    val rutNorm = normalizarRut(rut)
    val regex = "^[0-9]{7,8}-[0-9K]$".toRegex()
    return regex.matches(rutNorm)
}
