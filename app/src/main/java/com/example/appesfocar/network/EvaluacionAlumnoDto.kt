package com.example.appesfocar.network

import com.example.appesfocar.data.Rubrica
import com.example.appesfocar.util.normalizarRut

data class EvaluacionAlumnoDto(
    val id: Long? = null,
    val plantillaRubricaId: Long? = null,
    val tituloRubrica: String = "",
    val nombreAlumno: String = "",
    val apellidoAlumno: String = "",
    val rutAlumno: String = "",
    val puntaje1: Double = 0.0,
    val puntaje2: Double = 0.0,
    val puntaje3: Double = 0.0,
    val puntaje4: Double = 0.0,
    val feedbackDocente: String = "",
    val notaFinal: Double? = null,
    val estado: String? = null
)

fun EvaluacionAlumnoDto.toRubrica(): Rubrica {
    val notaCalc = this.notaFinal ?: ((this.puntaje1 + this.puntaje2 + this.puntaje3 + this.puntaje4) / 4.0)
    val estadoCalc = this.estado ?: if (notaCalc >= 4.0) "APROBADO" else "REPROBADO"
    val rutNorm = normalizarRut(this.rutAlumno)
    return Rubrica(
        id = this.id?.toInt() ?: 0,
        titulo = "Evaluación Alumno: ${this.nombreAlumno} ${this.apellidoAlumno}",
        tipo = "RUT: $rutNorm",
        nota = notaCalc,
        feedback = this.feedbackDocente,
        rutAlumno = rutNorm,
        nombreAlumno = this.nombreAlumno,
        apellidoAlumno = this.apellidoAlumno,
        tituloRubrica = if (this.tituloRubrica.isNotBlank()) this.tituloRubrica else "Evaluación Práctica ESFOCAR",
        notaFinal = notaCalc,
        estado = estadoCalc,
        feedbackDocente = this.feedbackDocente,
        plantillaRubricaId = this.plantillaRubricaId,
        puntaje1 = this.puntaje1,
        puntaje2 = this.puntaje2,
        puntaje3 = this.puntaje3,
        puntaje4 = this.puntaje4
    )
}

fun Rubrica.toEvaluacionAlumnoDto(): EvaluacionAlumnoDto {
    val notaCalc = if (this.notaFinal > 0.0) this.notaFinal else this.nota
    val estadoCalc = if (this.estado.isNotBlank()) this.estado else if (notaCalc >= 4.0) "APROBADO" else "REPROBADO"
    val rutNorm = normalizarRut(this.rutAlumno)
    return EvaluacionAlumnoDto(
        id = if (this.id > 0) this.id.toLong() else null,
        plantillaRubricaId = this.plantillaRubricaId,
        tituloRubrica = if (this.tituloRubrica.isNotBlank()) this.tituloRubrica else this.titulo,
        nombreAlumno = this.nombreAlumno,
        apellidoAlumno = this.apellidoAlumno,
        rutAlumno = rutNorm,
        puntaje1 = this.puntaje1,
        puntaje2 = this.puntaje2,
        puntaje3 = this.puntaje3,
        puntaje4 = this.puntaje4,
        feedbackDocente = if (this.feedbackDocente.isNotBlank()) this.feedbackDocente else this.feedback,
        notaFinal = notaCalc,
        estado = estadoCalc
    )
}
