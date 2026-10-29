package com.example.appesfocar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.launch

// Subimos la versión a 2 para reflejar los nuevos campos de Usuario (rol, cuentaActiva, fechaNacimiento, etc.)
@Database(entities = [Usuario::class, Rubrica::class], version = 2, exportSchema = false)
abstract class EsfocarDatabase : RoomDatabase() {

    // Conectamos los DAOs del proyecto
    abstract fun usuarioDao(): UsuarioDao
    abstract fun rubricaDao(): RubricaDao

    // Instancia ÚNICA (Singleton) para toda la aplicación
    companion object {
        @Volatile
        private var INSTANCE: EsfocarDatabase? = null

        fun getDatabase(context: Context): EsfocarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EsfocarDatabase::class.java,
                    "esfocar_database"
                )
                    .fallbackToDestructiveMigration()
                    // ⬇️ ESTE ES EL BLOQUE NUEVO PARA CREAR EL ADMIN POR DEFECTO ⬇️
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Ejecutamos la inserción en segundo plano apenas se crea la BD
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                val adminPorDefecto = Usuario(
                                    nombre = "Jefe",
                                    apellido = "Instructor",
                                    fechaNacimiento = "01/01/1980",
                                    correo = "admin@esfocar.cl", // Correo maestro
                                    contrasena = "123456",       // Contraseña maestra
                                    rol = "Admin",
                                    cuentaActiva = true          // El admin siempre nace activo
                                )
                                INSTANCE?.usuarioDao()?.registrarUsuario(adminPorDefecto)
                            }
                        }
                    })
                    // ⬆️ FIN DEL BLOQUE NUEVO ⬆️
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}