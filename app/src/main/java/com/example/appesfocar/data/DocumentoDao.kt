package com.example.appesfocar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DocumentoDao {

    @Query("SELECT * FROM documentos")
    suspend fun obtenerListaDocumentos(): List<Documento>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDocumento(documento: Documento)

    @Update
    suspend fun actualizarDocumento(documento: Documento)

    @Delete
    suspend fun eliminarDocumento(documento: Documento)
}
