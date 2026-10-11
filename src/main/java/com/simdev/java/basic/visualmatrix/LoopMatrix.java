package com.simdev.java.basic.visualmatrix;

import java.util.Arrays;

/**
 * Visualizador de bucles anidados en consola.
 * Representa gráficamente la estructura de ejecución de bucles for anidados,
 * mostrando las iteraciones en cada nivel y el progreso general.
 */
public class LoopMatrix {

    // Caracteres para representación visual de bucles
    private static final char EMPTY = '▯';
    private static final char FILLED = '▮';

    /**
     * Visualiza la estructura de un bucle for simple.
     *
     * @param loopNumber Número del bucle (1 para el exterior, 2 para el interior, etc.)
     * @param iteration  Iteración actual (0-indexed)
     * @param total      Total de iteraciones
     * @param indent     Nivel de indentación para visualización anidada
     */
    public static void visualizeSimpleLoop(int loopNumber, int iteration, int total, int indent) {
        String indentStr = repeatString(indent, "  ");
        double progress = total > 0 ? (double) iteration / total : 0.0;
        int barLength = (int) (progress * 20);
        barLength = Math.max(0, Math.min(barLength, 20));

        String bar = repeatChar(FILLED, barLength);
        String emptyBar = repeatChar(EMPTY, 20 - barLength);

        System.out.printf("%sBucle %d: [%s%s] %d/%d (%.1f%%)%n",
                indentStr, loopNumber, bar, emptyBar, iteration + 1, total, progress * 100);
    }

    /**
     * Visualiza bucles anidados con progreso en cada nivel.
     *
     * @param iterations   Array con el número actual de iteración en cada nivel
     * @param totals       Array con el total de iteraciones en cada nivel
     * @param operation    Descripción de la operación
     */
    public static void visualizeNestedLoops(int[] iterations, int[] totals, String operation) {
        System.out.println("\n=== Visualización de Bucles Anidados ===");
        System.out.println("Operación: " + operation);
        System.out.println();

        // Mostrar cada nivel
        int maxLevels = Math.min(iterations.length, 4); // Máximo 4 bucles para visualización clara
        for (int i = 0; i < maxLevels; i++) {
            int level = i + 1;
            int iteration = iterations[i];
            int total = totals[i];

            if (total <= 0) {
                System.out.println("Nivel " + level + ": --/-- (N/A)");
                continue;
            }

            double progress = (double) iteration / total;
            int barLength = (int) (progress * 30);
            barLength = Math.max(0, Math.min(barLength, 30));

            String filled = repeatChar(FILLED, barLength);
            String empty = repeatChar(EMPTY, 30 - barLength);

            System.out.printf("Nivel %d (for %d): [%s%s] %d/%d (%.1f%%)%n",
                    level, i, filled, empty, iteration + 1, total, progress * 100);
        }

        // Mostrar información de complejidad si tenemos 2 bucles
        if (iterations.length >= 2) {
            int totalOps = totals[0] * totals[1];
            System.out.println("Operaciones totales estimadas: " + totalOps);
            System.out.println("Complejidad temporal: O(" + totals[0] + " × " + totals[1] + ") = O(" + totalOps + ")");
        }

        System.out.println("─────────────────────────────────────────");
    }

    /**
     * Visualiza el cuerpo del bucle en ejecución con conteo de iteraciones.
     *
     * @param loopLevel      Número de nivel del bucle
     * @param currentIter    Iteración actual dentro de este nivel
     * @param outerIter      Iteración de los bucles externos
     * @param bodyOperation  Lo que ocurre en el cuerpo del bucle
     */
    public static void visualizeBodyExecution(int loopLevel, int currentIter, int outerIter,
                                              String bodyOperation) {
        System.out.printf("┌─ Bucle nivel %d │ iteración %d (externo: %d) │ %s%n",
                loopLevel, currentIter, outerIter, bodyOperation);
    }

    /**
     * Visualiza el retorno de un bucle con el valor acumulado o resultado.
     *
     * @param loopLevel      Número de nivel del bucle
     * @param result         Resultado final calculado
     * @param totalIterations Número total de iteraciones
     */
    public static void visualizeLoopResult(int loopLevel, int result, int totalIterations) {
        System.out.println("\n--- Resultado del Bucle " + loopLevel + " ---");
        System.out.println("Valor acumulado: " + result);
        System.out.println("Iteraciones totales: " + totalIterations);
        double average = totalIterations > 0 ? (double) result / totalIterations : 0;
        System.out.printf("Promedio por iteración: %.2f%n", average);
        System.out.println("─────────────────────────────────────");
    }

    /**
     * Visualiza la complejidad de un algoritmo basado en la estructura de bucles.
     *
     * @param numLoops       Número de bucles anidados
     * @param loopSizes      Tamaños de cada bucle
     * @param operationDescr Descripción de la operación
     */
    public static void visualizeComplexityAnalysis(int numLoops, int[] loopSizes, String operationDescr) {
        System.out.println("\n=== Análisis de Complejidad Temporal ===");
        System.out.println("Operación: " + operationDescr);
        System.out.println("Estructura: " + numLoops + " bucles anidados");

        // Calcular complejidad
        int totalOperations = 1;
        StringBuilder complexityExpr = new StringBuilder();

        for (int i = 0; i < numLoops; i++) {
            totalOperations *= loopSizes[i];
            if (i > 0) complexityExpr.append(" × ");
            complexityExpr.append(loopSizes[i]);
        }

        System.out.println("Operaciones totales: " + totalOperations);
        System.out.println("Notación Big O: O(" + complexityExpr + ")");

        // Categoría de complejidad
        String complexityClass = classifyComplexity(totalOperations);
        System.out.println("Clase de complejidad: " + complexityClass);

        System.out.println("─────────────────────────────────────────");
    }

    private static String classifyComplexity(int operations) {
        if (operations <= 100) return "Constant/Logarítmico (~O(1) - O(log n))";
        if (operations <= 1000) return "Lineal (~O(n))";
        if (operations <= 10000) return "Lineal-Logarítmico (~O(n log n))";
        if (operations <= 100000) return "Cuadrática (~O(n²))";
        if (operations <= 1000000) return "Cúbica (~O(n³))";
        return "Exponencial/Alta (~O(2^n) o peor)";
    }

    /**
     * Repite un carácter n veces.
     *
     * @param c Carácter a repetir
     * @param count Número de repeticiones
     * @return Cadena con el carácter repetido n veces
     */
    private static String repeatChar(char c, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    /**
     * Repite una cadena n veces.
     *
     * @param count Número de repeticiones
     * @param str   Cadena a repetir
     * @return Cadena con str repetido count veces
     */
    private static String repeatString(int count, String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}