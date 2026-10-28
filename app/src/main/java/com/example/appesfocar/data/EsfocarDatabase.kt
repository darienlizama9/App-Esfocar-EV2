package com.example.appesfocar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}