package com.example.appesfocar

import com.example.appesfocar.data.Rubrica
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstudianteUnitTest {

    // Prueba 1: Normalización de RUT (quitar espacios y mayúsculas)
    fun normalizarRut(rut: String): String {
        return rut.trim().uppercase()
    }

    // Prueba 2: Filtrado de evaluaciones por RUT del alumno
    fun filtrarEvaluacionesPorRut(lista: List<Rubrica>, rut: String): List<Rubrica> {
        return lista.filter { it.rutAlumno == rut || it.tipo.contains(rut) }
    }

    // Prueba 3: Validación de rol Estudiante
    fun esRolEstudiante(rol: String): Boolean {
        return rol.equals("Estudiante", ignoreCase = true)
    }

    @Test
    fun testNormalizarRutEstudiante() {
        val rutLimpio = normalizarRut(" 12345678-k ")
        assertEquals("12345678-K", rutLimpio)
    }

    @Test
    fun testFiltrarEvaluacionesPorRut() {
        val listaMock = listOf(
            Rubrica(id = 1, rutAlumno = "12345678-9", titulo = "Evaluación 1"),
            Rubrica(id = 2, rutAlumno = "98765432-1", titulo = "Evaluación 2")
        )
        val resultado = filtrarEvaluacionesPorRut(listaMock, "12345678-9")
        assertEquals(1, resultado.size)
        assertEquals(1, resultado[0].id)
    }

    @Test
    fun testValidarRolEstudiante() {
        assertTrue(esRolEstudiante("Estudiante"))
        assertTrue(esRolEstudiante("estudiante"))
    }
}