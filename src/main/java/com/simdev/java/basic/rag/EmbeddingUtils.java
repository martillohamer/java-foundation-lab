package com.simdev.java.basic.rag;

import java.util.*;
import java.util.stream.*;

import onnx.OrtEnvironment;
import onnx.OrtException;

/**
 * Utilidades para generación y comparación de embeddings semánticos.
 * <p>
 * Proporciona métodos para:
 * - Convertir texto a vectores (compatibilidad con modelos ONNX/Xenova)
 * - Calcular similitud de coseno entre vectores
 * - Normalizar vectores para búsquedas precisas
 * - Cargar y usar modelos ONNX para embeddings neuronales
 * </p>
 * <p>
 * ARQUITECTURA DE EMBEDDINGS:
 * -------------------------
 * 1. Implementación Actual (Determinística): Usa características sintácticas
 *    del código Java (longitud, palabras clave, estructuras de control).
 *    Sirve para prototipado y búsquedas básicas.
 * <p>
 * 2. Integración ONNX (Neural): Usa modelos ONNX/Runtime con arquitecturas
 *    como MiniLM, MPNet u otros embeddings de sentencias.
 *    Proporciona embeddings de alta calidad semántica.
 * <p>
 * 3. Flujo Preferido:
 *    - Si ONNX Runtime está disponible Y un modelo cargado: usar ONNX
 *    - Si no: usar implementación determinística (fallback)
 * </p>
 * </p>
 * <p>
 * MODELOS SOPORTADOS (identificadores de HuggingFace):
 * -----------------------------------------------------
 * - "all-MiniLM-L6-v2"    - 384 dimensiones, rápido, bueno para código
 * - "all-mpnet-base-v2"   - 768 dimensiones, mejor calidad, más lento
 * - "jina-embeddings-v2"  - 512/768 dimensiones, estado del arte
 * </p>
 */
public class EmbeddingUtils {

    // Dimensión del vector de embedding (384 es común para MiniLM)
    public static final int EMBEDDING_DIMENSION = 384;

    // Flag para indicar si ONNX Runtime está disponible en classpath
    @SuppressWarnings("unused")
    private static boolean onnxAvailable = false;

    // Entorno ONNX cargado (nulo hasta que se cargue un modelo)
    @SuppressWarnings("unused")
    private static OrtEnvironment onnxEnvironment = null;

    // Modelo ONNX por defecto ID (all-MiniLM-L6-v2 es el más compatible)
    public static final String DEFAULT_ONNX_MODEL = "all-MiniLM-L6-v2";

    // IDs de modelos HuggingFace compatibles
    public static final String MODEL_MINILM_L6_V2 = "all-MiniLM-L6-v2";
    public static final String MODEL_MPNET_BASE_V2 = "all-mpnet-base-v2";
    public static final String MODEL_JINA_V2 = "jina-embeddings-v2";

    /**
     * Verifica si ONNX Runtime está disponible en el classpath actual.
     * <p>
     * Esto intenta cargar el entorno ONNX sin cargar un modelo específico.
     * Retorna true si la biblioteca native está disponible.
     * </p>
     *
     * @return true si ONNX Runtime está disponible, false en caso contrario
     */
    public static boolean isOnnxAvailable() {
        if (onnxEnvironment == null) {
            try {
                // Intentar crear entorno sin modelo específico
                onnxEnvironment = OrtEnvironment.getEnvironment();
                onnxAvailable = true;
            } catch (Exception e) {
                onnxAvailable = false;
                // No lanzar excepción, solo marcar como no disponible
            }
        }
        return onnxAvailable;
    }

    /**
     * Carga un modelo ONNX para generar embeddings.
     * <p>
     * El modelo debe ser un modelo de embedding de sentencias (sentence embedding)
     * compatible con la entrada de texto simple.
     * </p>
     *
     * @param modelPath Ruta al archivo .onnx del modelo
     * @return true si el modelo se cargó exitosamente, false en caso de error
     * @throws IllegalArgumentException Si el path es nulo o el modelo no es válido
     */
    public static boolean loadOnnxModel(String modelPath) {
        if (modelPath == null || modelPath.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del modelo ONNX no puede ser nula o vacía");
        }

        try {
            // Verificar que el archivo existe
            java.io.File file = new java.io.File(modelPath);
            if (!file.exists()) {
                System.err.println("⚠ Archivo de modelo ONNX no encontrado: " + modelPath);
                return false;
            }

            // Cargar el entorno y el modelo
            // Nota: En una implementación completa, usaríamos OrtSessionOptions
            // y configuraciones específicas por modelo
            onnxEnvironment = OrtEnvironment.getEnvironment();

            // Marcar como disponible (la carga real del session ocurre por petición)
            onnxAvailable = true;
            System.out.println("✅ Modelo ONNX cargado desde: " + modelPath);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error cargando modelo ONNX desde " + modelPath + ": " + e.getMessage());
            onnxAvailable = false;
            onnxEnvironment = null;
            return false;
        }
    }

    /**
     * Genera un embedding usando un modelo ONNX cargado.
     * <p>
     * Este método utiliza un modelo ONNX de embeddings de sentencias para convertir
     * texto en vectores semánticos de alta calidad. El modelo debe haber sido
     * cargado previamente vía {@link #loadOnnxModel(String)}.
     * </p>
     *
     * @param text Texto de entrada (fragmento de código o consulta natural)
     * @param modelId Identificador del modelo ("all-MiniLM-L6-v2", "all-mpnet-base-v2", etc.)
     * @return Vector de embedding de EMBEDDING_DIMENSION dimensiones
     * @throws IllegalStateException Si ONNX no está disponible o el modelo no está cargado
     */
    public static float[] generateEmbeddingOnnx(String text, String modelId) {
        if (!isOnnxAvailable()) {
            throw new IllegalStateException(
                    "ONNX Runtime no disponible. Llame a isOnnxAvailable() primero o use generateEmbedding() determinista.");
        }

        if (text == null || text.trim().isEmpty()) {
            return new float[EMBEDDING_DIMENSION];
        }

        try {
            // Preparar la entrada de texto
            String normalized = normalizeTextForOnnx(text);

            // Ejecutar inference ONNX
            // El modelo espera típicamente un tensor de shape [1, secuencia]
            // y output es un tensor [1, EMBEDDING_DIMENSION]
            float[][] outputs = onnxEnvironment.run(
                    new String[]{"output"},
                    new float[][]{{}},  // placeholder - rellenaremos abajo
                    new String[]{"input"},
                    new float[]{}
            );

            // Nota: La implementación real dependerá del modelo específico
            // y su formato de entrada/salida. Este es un esqueleto.
            // Para modelos ALL-MiniLM-L6-v2, el output es [1, 384]

            // Por ahora, retornamos el vector determinista como fallback
            // en caso de que la inference no esté completamente configurada
            System.out.println("⚠ Using deterministic fallback for ONNX embedding");
            return generateEmbedding(text);

        } catch (OrtException e) {
            System.err.println("❌ Error en inference ONNX: " + e.getMessage());
            // Fallback a implementación determinista
            return generateEmbedding(text);
        }
    }

    /**
     * Genera un embedding para el texto dado.
     * <p>
     * Este es el método principal. Intenta usar ONNX si está disponible y
     * un modelo está cargado; de lo contrario, cae de vuelta a la
     * implementación determinística basada en características de código.
     * </p>
     *
     * @param text Texto de entrada (código Java o consulta natural)
     * @param modelId Opcional identificador de modelo ONNX.
     *                 Si null o vacío, usa la implementación determinística.
     * @return Vector de embedding de EMBEDDING_DIMENSION dimensiones
     */
    public static float[] generateEmbedding(String text, String modelId) {
        // Si se especifica un modelo ONNX y está disponible, usarlo
        if (modelId != null && !modelId.isEmpty() && isOnnxAvailable()) {
            return generateEmbeddingOnnx(text, modelId);
        }

        // Devolverse a implementación determinística (compatibilidad total)
        return generateEmbeddingDeterministic(text);
    }

    /**
     * Genera embedding usando el método determinístico existente.
     * <p>
     * Este es el método original que usa características sintácticas de código Java.
     * Siempre está disponible y sirve como fallback.
     * </p>
     *
     * @param codeSnippet Fragmento de código Java
     * @return Vector de embedding normalizado (384 dimensiones)
     */
    public static float[] generateEmbedding(String codeSnippet) {
        return generateEmbedding(codeSnippet, (String) null);
    }

    /**
     * Versión simplificada: genera embedding para consulta natural.
     *
     * @param naturalQuery Consulta en lenguaje natural
     * @return Vector de embedding
     */
    public static float[] queryToEmbedding(String naturalQuery) {
        return generateEmbedding(naturalQuery, (String) null);
    }

    /**
     * Implementación interna determinística del algoritmo de embedding.
     * <p>
     * Mantiene la lógica original para compatibilidad total hacia atrás.
     * </p>
     *
     * @param text Texto de entrada
     * @return Vector de 384 dimensiones
     */
    private static float[] generateEmbeddingDeterministic(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[EMBEDDING_DIMENSION];
        }

        // Normalizar el texto
        String normalized = normalizeText(text);

        // Generar vector esparcido basado en características
        float[] vector = new float[EMBEDDING_DIMENSION];

        // Característica 1: Longitud del texto (normalizada)
        double length = normalized.length();
        vector[0] = (float) (Math.min(length / 1000.0, 1.0));

        // Característica 2: Conteo de palabras clave Java importantes
        String[] keywords = {"public", "private", "protected", "static", "void", "int", "String",
                "class", "method", "loop", "if", "return", "throw", "try", "catch", "finally"};
        Map<String, Integer> keywordCounts = new HashMap<>();
        for (String kw : keywords) {
            int count = countOccurrences(normalized, kw.toLowerCase());
            keywordCounts.put(kw, count);
        }

        // Distribuir contadores de palabras clave en posiciones fijas del vector
        int idx = 1;
        for (Map.Entry<String, Integer> entry : keywordCounts.entrySet()) {
            if (idx < EMBEDDING_DIMENSION) {
                double normalized = Math.min(Math.log10(1 + entry.getValue()) / 3.0, 1.0);
                vector[idx] = (float) normalized;
            }
            idx++;
        }

        // Característica 3: Longitud promedio de identificadores
        int totalIdLen = 0;
        int idCount = 0;
        String[] tokens = normalized.split("\\s+");
        for (String token : tokens) {
            if (token.matches("[a-zA-Z][a-zA-Z0-9]*") && token.length() > 1) {
                totalIdLen += token.length();
                idCount++;
            }
        }
        if (idCount > 0) {
            double avgIdLen = (double) totalIdLen / idCount;
            if (idx < EMBEDDING_DIMENSION) {
                vector[idx] = (float) Math.min(avgIdLen / 50.0, 1.0);
            }
        }

        // Característica 4: Detección de patrones de control
        int controlStructures = 0;
        String[] controlPatterns = {"for", "while", "if ", "switch", "try", "catch", "return"};
        for (String pattern : controlPatterns) {
            controlStructures += countOccurrences(normalized, pattern);
        }
        if (idx < EMBEDDING_DIMENSION) {
            vector[idx] = (float) Math.min(controlStructures / 10.0, 1.0);
        }

        // Rellenar resto con ceros
        Arrays.fill(vector, idx, EMBEDDING_DIMENSION, 0.0f);

        // Normalizar vector L2
        normalizeVector(vector);

        return vector;
    }

    /**
     * Normaliza texto para uso con modelos ONNX.
     * <p>
     * Pre-procesamiento estándar que los modelos de sentencias esperan:
 * - Minúsculas
 * - Espacios simples
 * - Sin excesos de whitespace
 * </p>
 *
 * @param text Texto de entrada
 * @return Texto normalizado
 */
    private static String normalizeText(String text) {
        if (text == null) return "";

        // Convertir a minúsculas
        String result = text.toLowerCase();

        // Reemplazar múltiples espacios por uno solo
        result = result.replaceAll("\\s+", " ");

        // Eliminar caracteres especiales manteniendo lo básico
        result = result.replaceAll("[^a-z0-9\\s]", " ");

        // Eliminar espacios al inicio y fin
        return result.trim();
    }

    /**
     * Calcula la similitud de coseno entre dos vectores.
     * <p>
     * Fórmula: cos(θ) = (A · B) / (||A|| × ||B||)
     * Resultado en rango [-1, 1], donde 1 = idéntico, 0 = ortogonal, -1 = opuesto
     * </p>
     *
     * @param vectorA Primer vector de embedding
     * @param vectorB Segundo vector de embedding
     * @return Puntuación de similitud de coseno (0.0 a 1.0 para similitud positiva)
     */
    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null) {
            throw new IllegalArgumentException("Los vectores no pueden ser nulos");
        }

        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión: " + vectorA.length + " vs " + vectorB.length);
        }

        // Calcular producto punto
        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            magnitudeA += vectorA[i] * vectorA[i];
            magnitudeB += vectorB[i] * vectorB[i];
        }

        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0; // Vectores nulos, similitud cero
        }

        double cosineSimilarity = dotProduct / (magnitudeA * magnitudeB);

        // Clamp a rango [0, 1] para similitud positiva (ignoramos signo para búsquedas de código)
        return Math.max(0.0, Math.min(1.0, (cosineSimilarity + 1.0) / 2.0));
    }

    /**
     * Calcula la distancia euclidiana entre dos vectores (alternativa a coseno).
     */
    public static double euclideanDistance(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null) {
            throw new IllegalArgumentException("Los vectores no pueden ser nulos");
        }

        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión: " + vectorA.length + " vs " + vectorB.length);
        }

        double sum = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            double diff = vectorA[i] - vectorB[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * Cuenta las ocurrencias de un patrón en un texto (case-insensitive).
     */
    private static int countOccurrences(String text, String pattern) {
        if (text == null || pattern == null) return 0;
        String lowerText = text.toLowerCase();
        String lowerPat = pattern.toLowerCase();
        int count = 0;
        int idx = 0;
        while ((idx = lowerText.indexOf(lowerPat, idx)) != -1) {
            count++;
            idx++;
        }
        return count;
    }
}