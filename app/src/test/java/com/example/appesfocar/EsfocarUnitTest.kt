package com.example.appesfocar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class EsfocarUnitTest {

    fun validarRut(rut: String): Boolean {
        val regex = "^[0-9]{7,8}-[0-9kK]$".toRegex()
        return regex.matches(rut)
    }

    fun validarContrasena(contrasena: String): Boolean {
        return contrasena.length >= 6
    }

    fun validarNotaRango(nota: Double): Boolean {
        return nota in 1.0..7.0
    }

    fun calcularPromedioExacto(n1: Double, n2: Double, n3: Double, n4: Double): Double {
        val promedioRaw = (n1 + n2 + n3 + n4) / 4.0
        return BigDecimal(promedioRaw).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    fun determinarEstadoEvaluacion(notaFinal: Double): String {
        return if (notaFinal >= 4.0) "APROBADO" else "REPROBADO"
    }

    @Test
    fun testValidarRutChilenoValido() {
        assertTrue(validarRut("12345678-9"))
        assertTrue(validarRut("9876543-K"))
    }

    @Test
    fun testValidarRutChilenoInvalido() {
        assertFalse(validarRut("123456789"))
        assertFalse(validarRut("12.345.678-9"))
    }

    @Test
    fun testValidarContrasenaLongitudMinima() {
        assertTrue(validarContrasena("123456"))
        assertFalse(validarContrasena("12345"))
    }

    @Test
    fun testValidarNotaEnRangoValido() {
        assertTrue(validarNotaRango(1.0))
        assertTrue(validarNotaRango(4.5))
        assertTrue(validarNotaRango(7.0))
        assertFalse(validarNotaRango(0.9))
        assertFalse(validarNotaRango(7.1))
    }

    @Test
    fun testCalcularPromedioExactoDosDecimales() {
        val promedio = calcularPromedioExacto(5.5, 6.0, 4.2, 5.8)
        assertEquals(5.38, promedio, 0.001)
    }

    @Test
    fun testDeterminarEstadoEvaluacionAprobadoYReprobado() {
        assertEquals("APROBADO", determinarEstadoEvaluacion(4.0))
        assertEquals("APROBADO", determinarEstadoEvaluacion(6.5))
        assertEquals("REPROBADO", determinarEstadoEvaluacion(3.99))
    }
}
