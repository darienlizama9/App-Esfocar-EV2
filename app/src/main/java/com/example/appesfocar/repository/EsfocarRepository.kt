package com.example.appesfocar.repository

import com.example.appesfocar.data.Documento
import com.example.appesfocar.data.EsfocarDatabase
import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.data.Usuario
import com.example.appesfocar.network.EsfocarApiService
import com.example.appesfocar.network.RetrofitClient
import com.example.appesfocar.network.toEvaluacionAlumnoDto
import com.example.appesfocar.network.toRubrica
import com.example.appesfocar.util.normalizarRut

class EsfocarRepository(
    private val db: EsfocarDatabase,
    private val apiService: EsfocarApiService = RetrofitClient.apiService
) {
    suspend fun login(correo: String, contrasena: String): Usuario? {
        return try {
            val response = apiService.login(
                Usuario(
                    correo = correo.trim(),
                    contrasena = contrasena.trim(),
                    nombre = "",
                    apellido = "",
                    fechaNacimiento = ""
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val usuarioRemoto = response.body()!!
                val usuarioConRut = usuarioRemoto.copy(rut = normalizarRut(usuarioRemoto.rut))
                db.usuarioDao().registrarUsuario(usuarioConRut)
                usuarioConRut
            } else {
                db.usuarioDao().iniciarSesion(correo.trim(), contrasena.trim())
            }
        } catch (e: Exception) {
            db.usuarioDao().iniciarSesion(correo.trim(), contrasena.trim())
        }
    }

    suspend fun registro(usuario: Usuario): Usuario {
        val usuarioNormalizado = usuario.copy(
            correo = usuario.correo.trim(),
            rut = normalizarRut(usuario.rut)
        )
        return try {
            val response = apiService.registro(usuarioNormalizado)
            if (response.isSuccessful && response.body() != null) {
                val usuarioGuardado = response.body()!!
                val usuarioDef = usuarioGuardado.copy(rut = normalizarRut(usuarioGuardado.rut))
                db.usuarioDao().registrarUsuario(usuarioDef)
                usuarioDef
            } else {
                db.usuarioDao().registrarUsuario(usuarioNormalizado)
                usuarioNormalizado
            }
        } catch (e: Exception) {
            db.usuarioDao().registrarUsuario(usuarioNormalizado)
            usuarioNormalizado
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
            }
            db.documentoDao().obtenerListaDocumentos()
        } catch (e: Exception) {
            db.documentoDao().obtenerListaDocumentos()
        }
    }

    suspend fun guardarDocumento(documento: Documento): ResultadoSync<Documento> {
        return try {
            val response = apiService.guardarDocumento(documento)
            if (response.isSuccessful && response.body() != null) {
                val docGuardado = response.body()!!
                db.documentoDao().insertarDocumento(docGuardado)
                ResultadoSync.ExitoServidor(docGuardado, "Documento guardado en Servidor Render y respaldo Room")
            } else {
                db.documentoDao().insertarDocumento(documento)
                ResultadoSync.GuardadoLocalOffline(documento, "Documento guardado en respaldo local SQLite Room")
            }
        } catch (e: Exception) {
            db.documentoDao().insertarDocumento(documento)
            ResultadoSync.GuardadoLocalOffline(documento, "Modo Offline: Documento guardado en SQLite Room local")
        }
    }

    suspend fun editarDocumento(documento: Documento): ResultadoSync<Documento> {
        return try {
            val response = apiService.editarDocumento(documento.id, documento)
            if (response.isSuccessful && response.body() != null) {
                val docEditado = response.body()!!
                db.documentoDao().actualizarDocumento(docEditado)
                ResultadoSync.ExitoServidor(docEditado, "Documento actualizado en Servidor Render y Room")
            } else {
                db.documentoDao().actualizarDocumento(documento)
                ResultadoSync.GuardadoLocalOffline(documento, "Documento actualizado en respaldo local SQLite Room")
            }
        } catch (e: Exception) {
            db.documentoDao().actualizarDocumento(documento)
            ResultadoSync.GuardadoLocalOffline(documento, "Modo Offline: Documento actualizado en SQLite Room local")
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

    suspend fun evaluarAlumno(rubrica: Rubrica): ResultadoSync<Rubrica> {
        val rutNorm = normalizarRut(rubrica.rutAlumno)
        val rubricaNormalizada = rubrica.copy(
            rutAlumno = rutNorm,
            tipo = "RUT: $rutNorm"
        )
        val dto = rubricaNormalizada.toEvaluacionAlumnoDto()

        return try {
            val response = apiService.evaluarAlumno(dto)
            if (response.isSuccessful && response.body() != null) {
                val evaluacionRemotaDto = response.body()!!
                val rubricaRemota = evaluacionRemotaDto.toRubrica()
                db.rubricaDao().guardarRubrica(rubricaRemota)
                ResultadoSync.ExitoServidor(
                    data = rubricaRemota,
                    mensaje = "Sincronizado con éxito en el Servidor (PostgreSQL Neon) y respaldado en Room local."
                )
            } else {
                db.rubricaDao().guardarRubrica(rubricaNormalizada)
                ResultadoSync.GuardadoLocalOffline(
                    data = rubricaNormalizada,
                    mensaje = "Aviso: El servidor backend devolvió un código ${response.code()}. Guardado solo en respaldo local SQLite Room."
                )
            }
        } catch (e: Exception) {
            db.rubricaDao().guardarRubrica(rubricaNormalizada)
            ResultadoSync.GuardadoLocalOffline(
                data = rubricaNormalizada,
                mensaje = "Modo Offline: No hay conexión con el backend (${e.localizedMessage ?: "servidor no disponible"}). Guardado en SQLite Room local."
            )
        }
    }

    suspend fun obtenerEvaluacionesPorRut(rut: String): List<Rubrica> {
        val rutNorm = normalizarRut(rut)
        if (rutNorm.isBlank()) return emptyList()

        return try {
            val response = apiService.obtenerEvaluacionesPorRut(rutNorm)
            if (response.isSuccessful && response.body() != null) {
                val remotasDto = response.body()!!
                val remotasRubricas = remotasDto.map { it.toRubrica() }
                remotasRubricas.forEach { rub ->
                    db.rubricaDao().guardarRubrica(rub)
                }
                val enLocal = db.rubricaDao().obtenerRubricasPorRut(rutNorm)
                if (enLocal.isNotEmpty()) enLocal else remotasRubricas
            } else {
                db.rubricaDao().obtenerRubricasPorRut(rutNorm)
            }
        } catch (e: Exception) {
            db.rubricaDao().obtenerRubricasPorRut(rutNorm)
        }
    }

    suspend fun obtenerTodasLasEvaluaciones(): List<Rubrica> {
        return try {
            val response = apiService.obtenerEvaluaciones()
            if (response.isSuccessful && response.body() != null) {
                val remotasDto = response.body()!!
                val remotasRubricas = remotasDto.map { it.toRubrica() }
                remotasRubricas.forEach { rub ->
                    db.rubricaDao().guardarRubrica(rub)
                }
                val enLocal = db.rubricaDao().obtenerTodasLasRubricas()
                if (enLocal.isNotEmpty()) enLocal else remotasRubricas
            } else {
                db.rubricaDao().obtenerTodasLasRubricas()
            }
        } catch (e: Exception) {
            db.rubricaDao().obtenerTodasLasRubricas()
        }
    }
}
