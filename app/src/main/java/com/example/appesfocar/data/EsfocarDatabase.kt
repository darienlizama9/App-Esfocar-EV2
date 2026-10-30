package com.example.appesfocar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.launch

@Database(entities = [Usuario::class, Rubrica::class], version = 2, exportSchema = false)
abstract class EsfocarDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun rubricaDao(): RubricaDao

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
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                val adminPorDefecto = Usuario(
                                    nombre = "Jefe",
                                    apellido = "Instructor",
                                    fechaNacimiento = "01/01/1980",
                                    correo = "admin@esfocar.cl",
                                    contrasena = "123456",
                                    rol = "Admin",
                                    cuentaActiva = true
                                )
                                INSTANCE?.usuarioDao()?.registrarUsuario(adminPorDefecto)
                            }
                        }
                    })

                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}