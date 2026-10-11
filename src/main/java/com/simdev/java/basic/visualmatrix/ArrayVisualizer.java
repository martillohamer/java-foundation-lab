package com.simdev.java.basic.visualmatrix;

import java.util.Arrays;

/**
 * Visualizador de Arrays en consola usando representaciones ASCII.
 * Muestra cómo se mueven los elementos durante operaciones como:
 * - Inserción
 * - Eliminación
 * - Búsqueda
 * - Ordenamiento
 */
public class ArrayVisualizer {

    // Ancho máximo de la visualización del array
    private static final int MAX_VISUAL_WIDTH = 50;
    // Caracteres para la representación visual
    private static final char EMPTY_CHAR = '▯';
    private static final char FILLED_CHAR = '▮';
    private static final char HIGHLIGHT_CHAR = '▶';
    private static final char CURSOR_CHAR = '●';

    /**
     * Helper:Repite un carácter n veces.
     */
    private static String repeat(char c, int count) {
        return new String(new char[count]).replace("\0", String.valueOf(c));
    }

    /**
     * Visualiza un array completo con highlight en posiciones específicas.
     *
     * @param array            El array a visualizar
     * @param highlightIndices Índices resaltar (ej. índice actual, índice comparado)
     * @param cursorIndex      Índice del "cursor" actual
     * @param operation        Descripción de la operación que se está realizando
     * @param timeNanos        Tiempo de ejecución en nanosegundos (opcional)
     * @param complexity       Estimación de complejidad temporal (opcional)
     */
    public static void visualizeArray(int[] array, int[] highlightIndices, int cursorIndex,
                                      String operation, long timeNanos, String complexity) {
        System.out.println("\n=== Visualización de Array ===");
        System.out.println("Operación: " + operation);

        if (timeNanos > 0) {
            double micros = timeNanos / 1000.0;
            System.out.printf("Tiempo: %.3f μs%n", micros);
        }

        if (complexity != null && !complexity.isEmpty()) {
            System.out.println("Complejidad estimada: " + complexity);
        }

        // Calcular ancho visual (máximo 50 caracteres para arrays grandes)
        int displayWidth = Math.min(array.length, MAX_VISUAL_WIDTH);

        // Encabezado de índices
        System.out.print("     ");
        for (int i = 0; i < array.length; i++) {
            System.out.printf("%2d ", i % 100);
        }
        System.out.println();

        // Línea de valores
        System.out.print("  → ");
        for (int i = 0; i < array.length; i++) {
            int value = array[i];
            boolean isHighlighted = highlightContains(highlightIndices, i);
            boolean isCursor = (i == cursorIndex);

            if (isHighlighted && isCursor) {
                System.out.print(" " + HIGHLIGHT_CHAR + CURSOR_CHAR + " ");
            } else if (isHighlighted) {
                System.out.print(" " + HIGHLIGHT_CHAR + "  ");
            } else if (isCursor) {
                System.out.print(" " + CURSOR_CHAR + "  ");
            } else {
                System.out.print(" " + String.format("%2d", value) + " ");
            }
        }
        System.out.println();

        // Línea de barras de progreso
        System.out.print("     ");
        int barMaxWidth = 10;
        for (int i = 0; i < array.length; i++) {
            int barLength = Math.min(barMaxWidth, Math.max(0, Math.min(barMaxWidth, Math.abs(array[i]) / 10)));
            String bar = repeat(FILLED_CHAR, barLength);
            String empty = repeat(EMPTY_CHAR, barMaxWidth - barLength);
            System.out.print(" [" + bar + empty + "] ");
        }
        System.out.println();

        // Leyenda
        System.out.println("Leyenda: ▮ = valor vacío, ▮ = valor, ▶ = highlight, ● = cursor");
        System.out.println("─────────────────────────────────────────────────────────────");
    }

    /**
     * Verifica si un índice está en la lista de resaltado.
     */
    private static boolean highlightContains(int[] highlightIndices, int index) {
        if (highlightIndices == null) return false;
        for (int idx : highlightIndices) {
            if (idx == index) return true;
        }
        return false;
    }
}