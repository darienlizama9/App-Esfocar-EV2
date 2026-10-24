package com.example.appesfocar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Aquí le decimos a Room qué tablas incluir y qué versión es (muy importante)
@Database(entities = [Usuario::class, Rubrica::class], version = 1, exportSchema = false)
abstract class EsfocarDatabase : RoomDatabase() {

    // Conectamos los DAOs que creaste antes
    abstract fun usuarioDao(): UsuarioDao
    abstract fun rubricaDao(): RubricaDao

    // Este bloque 'companion object' asegura que solo exista UNA instancia de la base de datos
    // en toda la aplicación, ahorrando memoria y evitando crasheos.
    companion object {
        @Volatile
        private var INSTANCE: EsfocarDatabase? = null

        fun getDatabase(context: Context): EsfocarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EsfocarDatabase::class.java,
                    "esfocar_database" // El nombre del archivo SQLite en el celular
                )
                    // Esto destruye y recrea la BD si cambias las tablas y subes la versión
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}