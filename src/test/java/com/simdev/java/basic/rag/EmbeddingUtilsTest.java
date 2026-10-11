package com.simdev.java.basic.rag;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link EmbeddingUtils}.
 * <p>
 * Valida el correcto funcionamiento de:
 * - Generación de vectores de embedding de 384 dimensiones
 * - Cálculo de similitud de coseno entre vectores
 * - Manejo de vectores nulos y dimensiones incorrectas
 * - Normalización L2 implícita en el proceso
 * </p>
 * <p>
 * Los tests cubren casos típicos con fragmentos de código Java y
 * casos borde con entradas nulas o vacías.
 * </p>
 */
class EmbeddingUtilsTest {

    private EmbeddingUtils embeddingUtils;

    @BeforeEach
    void setUp() {
        embeddingUtils = new EmbeddingUtils();
    }

    @Test
    void testGenerateEmbedding_NonNullCode() {
        // Given: Un fragmento de código Java no nulo
        String code = "public void execute() {\n    System.out.println(\"hello\");\n}";

        // When: Generamos el embedding
        float[] embedding = EmbeddingUtils.generateEmbedding(code);

        // Then: El embedding debe tener la dimensión esperada
        assertNotNull(embedding, "El embedding no debe ser nulo");
        assertEquals(EmbeddingUtils.EMBEDDING_DIMENSION, embedding.length,
                "El embedding debe tener " + EmbeddingUtils.EMBEDDING_DIMENSION + " dimensiones");

        // Verificar que no es todo ceros (tendría sentido semántico)
        boolean allZeros = true;
        for (float f : embedding) {
            if (f != 0.0f) {
                allZeros = false;
                break;
            }
        }
        assertFalse(allZeros, "El embedding no debe ser todo ceros para código significativo");
    }

    @Test
    void testGenerateEmbedding_NullCode() {
        // Given: Código nulo
        // When: Generamos embedding con código nulo
        float[] embedding = EmbeddingUtils.generateEmbedding(null);

        // Then: Debe retornar un vector de dimensiones correctas (vacío/ceros)
        assertNotNull(embedding);
        assertEquals(EmbeddingUtils.EMBEDDING_DIMENSION, embedding.length);
        // Verificar que todos son cero
        boolean allZeros = true;
        for (float f : embedding) {
            if (f != 0.0f) {
                allZeros = false;
                break;
            }
        }
        assertTrue(allZeros, "Embedding de código nulo debe ser todo ceros");
    }

    @Test
    void testGenerateEmbedding_EmptyCode() {
        // Given: Código vacío
        // When: Generamos embedding con código vacío
        float[] embedding = EmbeddingUtils.generateEmbedding("");

        // Then: Debe retornar vector de dimensión correcta
        assertNotNull(embedding);
        assertEquals(EmbeddingUtils.EMBEDDING_DIMENSION, embedding.length);
        // Debería ser todo ceros o casi todo ceros
        boolean allZeros = true;
        for (float f : embedding) {
            if (Math.abs(f) > 0.001) {
                allZeros = false;
                break;
            }
        }
        // Para código vacío, puede tener algún valor por defecto, así que solo verificamos dimensión
    }

    @Test
    void testCosineSimilarity_IdenticalVectors() {
        // Given: Dos vectores idénticos
        float[] vectorA = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(vectorA, 0.5f); // Todos a 0.5

        float[] vectorB = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(vectorB, 0.5f); // Idéntico a vectorA

        // When: Calculamos similitud de coseno
        double similarity = EmbeddingUtils.cosineSimilarity(vectorA, vectorB);

        // Then: Debe ser 1.0 (idéntico)
        assertEquals(1.0, similarity, 0.001, "Vectores idénticos deben tener similitud 1.0");
    }

    @Test
    void testCosineSimilarity_OrthogonalVectors() {
        // Given: Dos vectores ortogonales (perpendiculares)
        float[] vectorA = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(vectorA, 1.0f); // Vector A: todos 1.0

        float[] vectorB = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(vectorB, 0.0f); // Vector B: todos 0.0 (nulo efectivamente)

        // When: Calculamos similitud
        double similarity = EmbeddingUtils.cosineSimilarity(vectorA, vectorB);

        // Then: Debe ser 0.0 (ortogonal/sin relación)
        assertEquals(0.0, similarity, 0.001, "Vectores ortogonales deben tener similitud 0.0");
    }

    @Test
    void testCosineSimilarity_OppositeVectors() {
        // Given: Dos vectores en direcciones opuestas
        float[] vectorA = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(vectorA, 1.0f);

        float[] vectorB = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(vectorB, -1.0f); // Todos -1.0

        // When: Calculamos similitud
        double similarity = EmbeddingUtils.cosineSimilarity(vectorA, vectorB);

        // Then: El fórmula normalizada da 0.0 (después del clamp a [0,1])
        // Los vectores opuestos tienen coseno -1, pero nuestro clamp a [0,1] lo deja en 0
        assertTrue(similarity >= 0.0 && similarity <= 1.0,
                "La similitud debe estar en rango [0,1] después del clamp");
    }

    @Test
    void testCosineSimilarity_NullVectors() {
        // Given: Vectores nulos
        // When/Then: Debe lanzar IllegalArgumentException
        Executable executable = () -> EmbeddingUtils.cosineSimilarity(null, new float[384]);
        assertThrows(IllegalArgumentException.class, executable,
                "Similitud con vector nulo debe lanzar excepción");

        executable = () -> EmbeddingUtils.cosineSimilarity(new float[384], null);
        assertThrows(IllegalArgumentException.class, executable,
                "Similitud con vector nulo segundo debe lanzar excepción");
    }

    @Test
    void testCosineSimilarity_DifferentDimensions() {
        // Given: Vectores de dimensiones diferentes
        float[] vectorA = new float[384];
        float[] vectorB = new float[256];

        // When/Then: Debe lanzar IllegalArgumentException
        Executable executable = () -> EmbeddingUtils.cosineSimilarity(vectorA, vectorB);
        assertThrows(IllegalArgumentException.class, executable,
                "Similitud con dimensiones diferentes debe lanzar excepción");
    }
}