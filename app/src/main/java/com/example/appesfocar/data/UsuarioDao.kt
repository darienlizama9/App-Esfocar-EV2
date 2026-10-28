package com.example.appesfocar.data



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE cuentaActiva = 0 AND rol = 'Docente'")
    suspend fun obtenerUsuariosPendientes(): List<Usuario>

    @Update
    suspend fun actualizarUsuario(usuario: Usuario)
}