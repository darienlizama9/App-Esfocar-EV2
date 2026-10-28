package com.example.appesfocar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documentos")
data class Documento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreArchivo: String,
    val rutaUri: String
)