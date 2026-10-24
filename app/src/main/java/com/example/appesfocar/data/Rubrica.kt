package com.example.appesfocar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rubricas")
data class Rubrica(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val tipo: String,
    val nota: Double,
    val feedback: String
)