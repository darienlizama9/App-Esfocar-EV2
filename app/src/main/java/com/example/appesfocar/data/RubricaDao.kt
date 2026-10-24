package com.example.appesfocar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RubricaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarRubrica(rubrica: Rubrica)

    @Query("SELECT * FROM rubricas")
    suspend fun obtenerTodasLasRubricas(): List<Rubrica>

    @Update
    suspend fun actualizarRubrica(rubrica: Rubrica)

    @Delete
    suspend fun eliminarRubrica(rubrica: Rubrica)
}