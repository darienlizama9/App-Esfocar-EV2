package com.example.appesfocar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {
    @Query("SELECT * FROM documentos")
    fun obtenerTodosLosDocumentos(): Flow<List<Documento>>

    @Insert
    suspend fun insertarDocumento(documento: Documento)

    @Update
    suspend fun actualizarDocumento(documento: Documento)

    @Delete
    suspend fun eliminarDocumento(documento: Documento)
}