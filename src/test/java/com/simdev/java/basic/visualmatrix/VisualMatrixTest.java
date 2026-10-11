package com.simdev.java.basic.visualmatrix;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el motor de visualización VisualMatrix.
 * Valida el funcionamiento de ArrayVisualizer y LoopMatrix.
 */
public class VisualMatrixTest {

    @Test
    @DisplayName("Test: Visualizar array vacío")
    void givenEmptyArray_whenVisualizeArray_thenNoCrash() {
        // Arrange
        int[] emptyArray = {};
        
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> 
            ArrayVisualizer.visualizeArray(emptyArray, null, -1, "Test", 0, "O(1)")
        );
    }

    @Test
    @DisplayName("Test: Visualizar array con elementos")
    void givenPopulatedArray_whenVisualizeArray_thenSuccess() {
        // Arrange
        int[] testArray = {10, 20, 30, 40, 50};
        
        // Act & Assert
        assertDoesNotThrow(() ->
            ArrayVisualizer.visualizeArray(testArray, new int[]{1, 3}, 2,
                    "Ordenamiento", System.nanoTime(), "O(n log n)")
        );
    }

    @Test
    @DisplayName("Test: Visualizar array con highlight y cursor")
    void givenArrayWithHighlight_whenVisualizeArray_thenHighlightShown() {
        // Arrange
        int[] testArray = {5, 15, 25, 35, 45};
        
        // Act & Assert - no debe lanzar excepción
        assertDoesNotThrow(() ->
            ArrayVisualizer.visualizeArray(testArray, new int[]{0, 4}, 2,
                    "Búsqueda", -1, "O(n)")
        );
    }

    @Test
    @DisplayName("Test: Visualizar bucle simple")
    void givenValidLoop_whenVisualizeSimpleLoop_thenSuccess() {
        // Act & Assert
        assertDoesNotThrow(() ->
            LoopMatrix.visualizeSimpleLoop(1, 5, 10, 0)
        );
    }

    @Test
    @DisplayName("Test: Visualizar bucle interno")
    void givenNestedLoop_whenVisualizeNestedLoops_thenSuccess() {
        // Arrange
        int[] iterations = {10, 5};
        int[] totals = {20, 10};
        
        // Act & Assert
        assertDoesNotThrow(() ->
            LoopMatrix.visualizeNestedLoops(iterations, totals, "Producto de matrices")
        );
    }

    @Test
    @DisplayName("Test: Visualizar complejidad temporal")
    void givenComplexityAnalysis_whenVisualizeComplexityAnalysis_thenSuccess() {
        // Act & Assert
        assertDoesNotThrow(() ->
            LoopMatrix.visualizeComplexityAnalysis(2, new int[]{5, 10}, "Búsqueda de par")
        );
    }

    @Test
    @DisplayName("Test: Medir tiempo de ejecución")
    void givenRunnable_whenMeasureAndVisualize_thenTrackTime() {
        // Act & Assert
        assertDoesNotThrow(() ->
            VisualMatrix.measureAndVisualize(() -> {
                // Simular trabajo
                int sum = 0;
                for (int i = 0; i < 100; i++) {
                    sum += i;
                }
                return sum;
            }, "Suma de 100 elementos")
        );
    }

    @Test
    @DisplayName("Test: Medir tiempo con callable")
    void givenCallable_whenMeasureAndVisualizeCallable_thenTrackTime() {
        // Act & Assert
        assertDoesNotThrow(() ->
            VisualMatrix.measureAndVisualize(() -> 42, "Valor constante")
        );
    }

    @Test
    @DisplayName("Test: Reporte de rendimiento múltiple")
    void givenPerformanceReport_whenVisualizePerformanceReport_thenSuccess() {
        // Act & Assert
        long[][] times = {
            {1000, 1050, 980},
            {2000, 1950, 2030},
            {1500, 1520, 1490}
        };
        
        assertDoesNotThrow(() ->
            VisualMatrix.visualizePerformanceReport(
                    java.util.Arrays.asList("Operación A", "Operación B", "Operación C"),
                    times
            )
        );
    }

    @Test
    @DisplayName("Test: Visualizar resultado de bucle")
    void givenLoopResult_whenVisualizeLoopResult_thenSuccess() {
        // Act & Assert
        assertDoesNotThrow(() ->
            VisualMatrix.visualizeLoopResult(1, 150, 50)
        );
    }

    @Test
    @DisplayName("Test: Visualizar cuerpo de bucle")
    void givenBodyExecution_whenVisualizeBodyExecution_thenSuccess() {
        // Act & Assert
        assertDoesNotThrow(() ->
            VisualMatrix.visualizeBodyExecution(2, 5, 3, "Iterando en segundo nivel")
        );
    }
}