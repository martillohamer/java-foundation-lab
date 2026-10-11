package com.simdev.java.basic.visualmatrix;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Motor de visualización de algoritmos y estructuras de datos.
 <p>
 * Proporciona capacidades visuales en consola para:
 * - Visualización de arrays con resaltado de operaciones
 - Visualización de bucles anidados con progreso en tiempo real
 - Métricas de rendimiento (tiempo en nanosegundos, estimación de complejidad)
 - Seguimiento paso a paso de algoritmos comunes
 </p>
 */
public class VisualMatrix {

    // Constantes de configuración visual
    private static final String SEPARATOR = "━".repeat(50);
    private static final String HEADER_PREFIX = "╔";
    private static final String FOOTER_PREFIX = "╚";

    // Contador de pasos global para algoritmos
    private static int stepCounter = 0;

    /**
     * Inicia una sesión de visualización para un algoritmo específico.
     *
     * @param algorithmName Nombre del algoritmo que se está visualizando
     */
    public static void startSession(String algorithmName) {
        stepCounter = 0;
        System.out.println("\n" + SEPARATOR);
        System.out.println("INICIANDO SESIÓN DE VISUALIZACIÓN: " + algorithmName.toUpperCase());
        System.out.println(SEPARATOR + "\n");
    }

    /**
     * Finaliza la sesión de visualización mostrando resumen.
     *
     * @param totalSteps      Número total de pasos ejecutados
     * @param executionTimeNs Tiempo total de ejecución en nanosegundos
     */
    public static void endSession(int totalSteps, long executionTimeNs) {
        double totalMs = executionTimeNs / 1_000_000.0;
        System.out.println("\n" + SEPARATOR);
        System.out.println("SESION FINALIZADA");
        System.out.println("Pasos totales: " + totalSteps);
        System.out.printf("Tiempo total: %.3f ms (%.0f ns)%n", totalMs, executionTimeNs);
        System.out.println("Tiempo promedio por paso: " + (totalSteps > 0 ? String.format("%.2f", executionTimeNs / totalSteps) + " ns" : "N/A"));
        System.out.println(SEPARATOR + "\n");
    }

    /**
     * Visualiza un array con resaltado paso a paso.
     * <p>
     * Ejemplo:
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * Visualización de Array
     * Operación: Intercambio de elementos
     * Tiempo: 0.123 μs
     * Complejidad estimada: O(1)
     * ┌─────┬─────┬─────┬─────┬─────┐
     * │  42 │  17 │  99 │  ▶  │  ▯  │
     * └─────┴─────┴─────┴─────┴─────┘
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * </p>
     *
     * @param array           El array a visualizar
     * @param highlightPos    Posiciones resaltar (puede ser null)
     * @param cursorPos       Posición del cursor/actual
     * @param operationDesc   Descripción de la operación
     * @param timeNanos       Tiempo de ejecución en nanosegundos
     * @param complexityDesc  Estimación de complejidad temporal
     */
    public static void visualizeArrayStep(int[] array, int[] highlightPos, int cursorPos, 
                                          String operationDesc, long timeNanos, String complexityDesc) {
        stepCounter++;
        System.out.println("\n--- Paso " + stepCounter + " ---");
        System.out.println("Operación: " + operationDesc);

        if (timeNanos >= 0) {
            double micros = timeNanos / 1000.0;
            System.out.printf("Tiempo: %.3f μs%n", micros);
        }

        if (complexityDesc != null && !complexityDesc.isEmpty()) {
            System.out.println("Complejidad: " + complexityDesc);
        }

        // Visualizar usando el ArrayVisualizer interno
        ArrayVisualizer.visualizeArray(array, highlightPos, cursorPos, 
                operationDesc, timeNanos, complexityDesc);
    }

    /**
     * Visualiza la estructura de bucles anidados con progreso.
     *
     * @param iterations      Iteración actual en cada nivel
     * @param totals          Total de iteraciones en cada nivel
     * @param operationDesc   Descripción de la operación del bucle
     */
    public static void visualizeNestedLoops(int[] iterations, int[] totals, String operationDesc) {
        stepCounter++;
        System.out.println("\n--- Paso " + stepCounter + " de bucles ---");
        System.out.println("Operación: " + operationDesc);
        LoopMatrix.visualizeNestedLoops(iterations, totals, operationDesc);
    }

    /**
     * Visualiza el resultado final de un bucle.
     *
     * @param loopLevel       Número del bucle
     * @param result          Resultado calculado
     * @param totalIterations Iteraciones totales
     */
    public static void visualizeLoopResult(int loopLevel, int result, int totalIterations) {
        stepCounter++;
        LoopMatrix.visualizeLoopResult(loopLevel, result, totalIterations);
    }

    /**
     * Mide el tiempo de ejecución de una operación y la visualiza.
     * <p>
     * Este método usa System.nanoTime() para medir con precisión y
     * proporciona una visualización en tiempo real del progreso.
     * </p>
     *
     * @param operation Runnable que representa la operación a medir
     * @param description Descripción de la operación para la visualización
     * @param <T>         Tipo de resultado de la operación (puede ser null)
     * @return El resultado de la operación, o null si era void
     */
    public static <T> T measureAndVisualize(Runnable operation, String description) {
        long startTime = System.nanoTime();
        
        operation.run();
        
        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        
        // Visualizar el tiempo medido
        visualizeOperationTime(description, durationNs);
        
        return null;
    }

    /**
     * Mide y visualiza una función que retorna un valor.
     *
     * @param task Callable-like tarea que retorna un resultado
     * @param description Descripción de la tarea
     * @param <T> Tipo de retorno
     * @return El resultado de la tarea
     */
    public static <T> T measureAndVisualize( java.util.concurrent.Callable<T> task, String description) {
        long startTime = System.nanoTime();
        
        T result;
        try {
            result = task.call();
        } catch (Exception e) {
            result = null;
        }
        
        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        
        visualizeOperationTime(description, durationNs);
        return result;
    }

    /**
     * Visualiza el tiempo de operación medido con formato profesional.
     *
     * @param operationDesc Descripción de la operación
     * @param durationNs    Duración en nanosegundos
     */
    private static void visualizeOperationTime(String operationDesc, long durationNs) {
        String unit;
        double value;
        
        if (durationNs < 1_000) {
            unit = "ns";
            value = durationNs;
        } else if (durationNs < 1_000_000) {
            unit = "μs";
            value = durationNs / 1000.0;
        } else if (durationNs < 1_000_000_000) {
            unit = "ms";
            value = durationNs / 1_000_000.0;
        } else {
            unit = "s";
            value = durationNs / 1_000_000_000.0;
        }
        
        System.out.printf("%s completado en %.3f %s%n", operationDesc, value, unit);
    }

    /**
     * Visualiza la estimación de complejidad temporal basada en el número de operaciones.
     *
     * @param operationCount Número de operaciones realizadas
     * @param inputSize      Tamaño de entrada (para algoritmos que dependen del tamaño)
     */
    public static void visualizeComplexityEstimate(long operationCount, int inputSize) {
        System.out.println("\n=== Estimación de Complejidad Temporal ===");
        System.out.println("Número de operaciones: " + operationCount);
        System.out.println("Tamaño de entrada: " + inputSize);

        // Calcular notación Big O aproximada
        double opsPerElement = inputSize > 0 ? (double) operationCount / inputSize : 0;

        if (operationCount <= inputSize) {
            System.out.println("Clasificación: O(1) - O(log n) - Constantemente bajo");
        } else if (operationCount <= inputSize * 10) {
            System.out.println("Clasificación: O(n) - Lineal");
        } else if (operationCount <= inputSize * inputSize) {
            System.out.println("Clasificación: O(n²) - Cuadrática");
        } else if (operationCount <= Math.pow(inputSize, 3)) {
            System.out.println("Clasificación: O(n³) - Cúbica");
        } else {
            System.out.println("Clasificación: Exponencial - O(2^n) o peor");
        }

        System.out.printf("Operaciones por elemento: %.2f%n", opsPerElement);
        System.out.println("─────────────────────────────────────────");
    }

    /**
     * Genera un reporte estadístico de múltiples ejecuciones de un algoritmo.
     *
     * @param operationNames Nombres de las operaciones medidas
     * @param timesNanos     Arrays de tiempos en nanosegundos para cada operación
     */
    public static void visualizePerformanceReport(List<String> operationNames, long[][] timesNanos) {
        System.out.println("\n=== REPORTE DE RENDIMIENTO ===");
        
        if (operationNames == null || timesNanos == null || timesNanos.length == 0) {
            System.out.println("No hay datos de rendimiento que reportar.");
            return;
        }

        // Encontrar el tiempo máximo, mínimo y promedio
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        for (long[] timeArray : timesNanos) {
            for (long t : timeArray) {
                totalTime += t;
                if (t < minTime) minTime = t;
                if (t > maxTime) maxTime = t;
            }
        }

        long avgTime = timesNanos.length > 0 ? totalTime / timesNanos.length : 0;

        System.out.println("Tiempo promedio: " + avgTime + " ns");
        System.out.println("Tiempo mínimo: " + minTime + " ns");
        System.out.println("Tiempo máximo: " + maxTime + " ns");
        System.out.println("Rango de variación: " + (maxTime - minTime) + " ns");
        System.out.println("─────────────────────────────────────────");
    }

    /**
     * Método principal para pruebas rápidas del visualizador.
     *
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        System.out.println("=== VisualMatrix Engine Demo ===\n");

        // Demo 1: Visualizar array
        startSession("Visualización de Array - Ejemplo");
        
        int[] demoArray = {34, 7, 23, 32, 5, 62};
        visualizeArrayStep(demoArray, new int[]{1, 3}, 2, 
                "Inicialización y resaltado de índices", 
                System.nanoTime(), "O(1) setup");

        // Demo 2: Bucle anidado
        visualizeNestedLoops(new int[]{25, 15}, new int[]{50, 20}, 
                "Bucle externo x Bucle interno - Demo");
        
        // Demo 3: Complejidad
        visualizeComplexityEstimate(1000, 100);
        
        // Demo 4: Reporte de rendimiento
        long[][] times = {{1500, 1520, 1480}, {2000, 1980, 2010}, {1800, 1820, 1790}};
        visualizePerformanceReport(
                Arrays.asList("Ordenamiento Burbuja", "Ordenamiento Inserción", "Búsqueda Binaria"),
                times
        );

        // Demo 5: Medir operación lambda
        TemporalDemo demo = new TemporalDemo();
        measureAndVisualize(() -> demo.computeSum(10000), "Cálculo de suma 10000 elementos");

        endSession(5, System.nanoTime() - System.nanoTime()); // Simplified for demo
        
        System.out.println("\n=== VisualMatrix Demo Completa ===\n");
    }

    /**
     * Clase interna de demo temporal para el main.
     */
    private static class TemporalDemo {
        public int computeSum(int n) {
            int sum = 0;
            for (int i = 0; i < n; i++) {
                sum += i;
            }
            return sum;
        }
    }
}