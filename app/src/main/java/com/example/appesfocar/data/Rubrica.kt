package com.example.appesfocar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rubricas")
data class Rubrica(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String = "",
    val tipo: String = "",
    val nota: Double = 0.0,
    val feedback: String = "",
    val rutAlumno: String = "",
    val nombreAlumno: String = "",
    val apellidoAlumno: String = "",
    val tituloRubrica: String = "",
    val notaFinal: Double = 0.0,
    val estado: String = "",
    val feedbackDocente: String = ""
)
