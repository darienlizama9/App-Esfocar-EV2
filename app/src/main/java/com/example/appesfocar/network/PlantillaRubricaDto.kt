package com.example.appesfocar.network

data class PlantillaRubricaDto(
    val id: Long? = null,
    val titulo: String = "",
    val tipo: String = "Individual",
    val criterio1Nombre: String = "",
    val criterio2Nombre: String = "",
    val criterio3Nombre: String = "",
    val criterio4Nombre: String = "",
    val puntajeMaximo: Double = 16.0
)
