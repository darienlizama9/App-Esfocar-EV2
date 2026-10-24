package com.example.appesfocar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarUsuario(usuario: Usuario)

    // Consulta SQL pura para revisar si el correo y la contraseña coinciden en el Login
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena")
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario?
}