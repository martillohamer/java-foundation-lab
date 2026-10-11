package com.simdev.java.basic.c02_operators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OperatorsTest {

    @Test
       @DisplayName("Prueba suma de dos números enteros")
    void testSuma() {
        int a = 5;
        int b = 3;
        int resultado = a + b;
        assertEquals(8, resultado, "La suma de 5 + 3 debe ser 8");
    }

    @Test
    @DisplayName("Prueba resta y validación con assertTrue")
    void testResta() {
        int a = 10;
        int b = 4;
        int resultado = a - b;
        assertTrue(resultado > 0, "El resultado de 10 - 4 debe ser mayor que 0");
        assertEquals(6, resultado, "10 - 4 debe igualar 6");
    }

    @Test
    @DisplayName("Prueba multiplicación")
    void testMultiplicacion() {
        int a = 6;
        int b = 7;
        int resultado = a * b;
        assertEquals(42, resultado, "6 * 7 debe ser 42");
    }

    @Test
    @DisplayName("Prueba división entera")
    void testDivision() {
        int a = 10;
        int b = 2;
        int resultado = a / b;
        assertEquals(5, resultado, "10 / 2 debe ser 5");
    }

    @Test
    @DisplayName("Prueba operador lógico AND")
    void testOperadorAND() {
        boolean condicion1 = true;
        boolean condicion2 = true;
        boolean resultado = condicion1 && condicion2;
        assertTrue(resultado, "true AND true debe ser true");
    }

    @Test
    @DisplayName("Prueba operador lógico OR")
    void testOperadorOR() {
        boolean condicion1 = false;
        boolean condicion2 = true;
        boolean resultado = condicion1 || condicion2;
        assertTrue(resultado, "false OR true debe ser true");
    }

    @Test
    @DisplayName("Prueba operador lógico NOT")
    void testOperadorNOT() {
        boolean condicion = true;
        boolean resultado = !condicion;
        assertFalse(resultado, "NOT true debe ser false");
    }
}