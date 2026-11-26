package com.example.appesfocar.network

import com.example.appesfocar.data.Documento
import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.data.Usuario
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EsfocarApiService {

    @POST("api/usuarios/login")
    suspend fun login(@Body usuario: Usuario): Response<Usuario>

    @POST("api/usuarios/registro")
    suspend fun registro(@Body usuario: Usuario): Response<Usuario>

    @GET("api/usuarios")
    suspend fun obtenerUsuarios(): Response<List<Usuario>>

    @GET("api/usuarios/pendientes")
    suspend fun obtenerUsuariosPendientes(): Response<List<Usuario>>

    @PUT("api/usuarios/{id}/aprobar")
    suspend fun aprobarUsuario(@Path("id") id: Int): Response<ResponseBody>

    @PUT("api/usuarios/{id}/rol")
    suspend fun cambiarRolUsuario(@Path("id") id: Int, @Query("rol") rol: String): Response<ResponseBody>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int): Response<ResponseBody>

    @GET("api/documentos")
    suspend fun obtenerDocumentos(): Response<List<Documento>>

    @POST("api/documentos")
    suspend fun guardarDocumento(@Body documento: Documento): Response<Documento>

    @PUT("api/documentos/{id}")
    suspend fun editarDocumento(@Path("id") id: Int, @Body documento: Documento): Response<Documento>

    @DELETE("api/documentos/{id}")
    suspend fun eliminarDocumento(@Path("id") id: Int): Response<ResponseBody>

    @GET("api/rubricas/plantillas")
    suspend fun obtenerPlantillasRubrica(): Response<List<Rubrica>>

    @POST("api/rubricas/plantillas")
    suspend fun guardarPlantillaRubrica(@Body rubrica: Rubrica): Response<Rubrica>

    @PUT("api/rubricas/plantillas/{id}")
    suspend fun editarPlantillaRubrica(@Path("id") id: Int, @Body rubrica: Rubrica): Response<Rubrica>

    @DELETE("api/rubricas/plantillas/{id}")
    suspend fun eliminarPlantillaRubrica(@Path("id") id: Int): Response<ResponseBody>

    @POST("api/rubricas/evaluar")
    suspend fun evaluarAlumno(@Body evaluacion: EvaluacionAlumnoDto): Response<EvaluacionAlumnoDto>

    @GET("api/rubricas/evaluaciones")
    suspend fun obtenerEvaluaciones(): Response<List<EvaluacionAlumnoDto>>

    @PUT("api/rubricas/evaluaciones/{id}")
    suspend fun editarEvaluacion(@Path("id") id: Int, @Body evaluacion: EvaluacionAlumnoDto): Response<EvaluacionAlumnoDto>

    @GET("api/rubricas/alumno/{rut}")
    suspend fun obtenerEvaluacionesPorRut(@Path("rut") rut: String): Response<List<EvaluacionAlumnoDto>>
}
