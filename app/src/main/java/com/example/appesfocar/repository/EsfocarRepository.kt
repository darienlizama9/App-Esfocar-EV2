package com.example.appesfocar.repository

import com.example.appesfocar.data.Documento
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.data.Usuario
import com.example.appesfocar.network.EsfocarApiService
import com.example.appesfocar.network.RetrofitClient

class EsfocarRepository(
    private val db: EsfocarDatabase,
    private val apiService: EsfocarApiService = RetrofitClient.apiService
) {
    suspend fun login(correo: String, contrasena: String): Usuario? {
        return try {
            val response = apiService.login(
                Usuario(
                    correo = correo,
                    contrasena = contrasena,
                    nombre = "",
                    apellido = "",
                    fechaNacimiento = ""
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val usuarioRemoto = response.body()!!
                db.usuarioDao().registrarUsuario(usuarioRemoto)
                usuarioRemoto
            } else {
                db.usuarioDao().iniciarSesion(correo, contrasena)
            }
        } catch (e: Exception) {
            db.usuarioDao().iniciarSesion(correo, contrasena)
        }
    }

    suspend fun registro(usuario: Usuario): Usuario {
        return try {
            val response = apiService.registro(usuario)
            if (response.isSuccessful && response.body() != null) {
                val usuarioGuardado = response.body()!!
                db.usuarioDao().registrarUsuario(usuarioGuardado)
                usuarioGuardado
            } else {
                db.usuarioDao().registrarUsuario(usuario)
                usuario
            }
        } catch (e: Exception) {
            db.usuarioDao().registrarUsuario(usuario)
            usuario
        }
    }

    suspend fun listarDocumentos(): List<Documento> {
        return try {
            val response = apiService.obtenerDocumentos()
            if (response.isSuccessful && response.body() != null) {
                val remotos = response.body()!!
                remotos.forEach { doc ->
                    db.documentoDao().insertarDocumento(doc)
                }
                remotos
            } else {
                db.documentoDao().obtenerListaDocumentos()
            }
        } catch (e: Exception) {
            db.documentoDao().obtenerListaDocumentos()
        }
    }

    suspend fun guardarDocumento(documento: Documento): Documento {
        return try {
            val response = apiService.guardarDocumento(documento)
            if (response.isSuccessful && response.body() != null) {
                val docGuardado = response.body()!!
                db.documentoDao().insertarDocumento(docGuardado)
                docGuardado
            } else {
                db.documentoDao().insertarDocumento(documento)
                documento
            }
        } catch (e: Exception) {
            db.documentoDao().insertarDocumento(documento)
            documento
        }
    }

    suspend fun editarDocumento(documento: Documento): Documento {
        return try {
            val response = apiService.editarDocumento(documento.id, documento)
            if (response.isSuccessful && response.body() != null) {
                val docEditado = response.body()!!
                db.documentoDao().actualizarDocumento(docEditado)
                docEditado
            } else {
                db.documentoDao().actualizarDocumento(documento)
                documento
            }
        } catch (e: Exception) {
            db.documentoDao().actualizarDocumento(documento)
            documento
        }
    }

    suspend fun eliminarDocumento(documento: Documento) {
        try {
            val response = apiService.eliminarDocumento(documento.id)
            if (response.isSuccessful) {
                db.documentoDao().eliminarDocumento(documento)
            } else {
                db.documentoDao().eliminarDocumento(documento)
            }
        } catch (e: Exception) {
            db.documentoDao().eliminarDocumento(documento)
        }
    }

    suspend fun evaluarAlumno(rubrica: Rubrica): Rubrica {
        return try {
            val response = apiService.evaluarAlumno(rubrica)
            if (response.isSuccessful && response.body() != null) {
                val evaluacionRemota = response.body()!!
                db.rubricaDao().guardarRubrica(evaluacionRemota)
                evaluacionRemota
            } else {
                db.rubricaDao().guardarRubrica(rubrica)
                rubrica
            }
        } catch (e: Exception) {
            db.rubricaDao().guardarRubrica(rubrica)
            rubrica
        }
    }

    suspend fun obtenerEvaluacionesPorRut(rut: String): List<Rubrica> {
        return try {
            val response = apiService.obtenerEvaluacionesPorRut(rut)
            if (response.isSuccessful && response.body() != null) {
                val remotas = response.body()!!
                remotas.forEach { rub ->
                    db.rubricaDao().guardarRubrica(rub)
                }
                remotas
            } else {
                db.rubricaDao().obtenerRubricasPorRut(rut)
            }
        } catch (e: Exception) {
            db.rubricaDao().obtenerRubricasPorRut(rut)
        }
    }

    suspend fun obtenerTodasLasEvaluaciones(): List<Rubrica> {
        return try {
            val response = apiService.obtenerEvaluaciones()
            if (response.isSuccessful && response.body() != null) {
                val remotas = response.body()!!
                remotas.forEach { rub ->
                    db.rubricaDao().guardarRubrica(rub)
                }
                remotas
            } else {
                db.rubricaDao().obtenerTodasLasRubricas()
            }
        } catch (e: Exception) {
            db.rubricaDao().obtenerTodasLasRubricas()
        }
    }
}
