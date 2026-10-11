package com.simdev.java.basic.rag;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link VectorStore}.
 * <p>
 * Valida el correcto funcionamiento de:
 * - Almacenamiento y recuperación de vectores
 * - Búsqueda por similitud de coseno
 * - Filtros por fase y tipo de elemento
 * - Gestión del tamaño del almacén
 * - Operaciones de limpieza y verificación
 * </p>
 * <p>
 * Los tests crean un VectorStore vacío y lo poblan manualmente con
 * entradas de prueba usando reflección para acceder a métodos package-private,
 * o bien prueban los métodos públicos disponibles.
 * </p>
 */
class VectorStoreTest {

    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        vectorStore = new VectorStore();
    }

    @Test
    void testAddAndGetSize() {
        // Given: Un VectorStore vacío
        // When: Añadimos entradas
        vectorStore.addEntry(generateTestEmbedding(0), "Test code snippet 1", createTestMetadata("ClassA", "CLASS", 1));
        vectorStore.addEntry(generateTestEmbedding(1), "Test code snippet 2", createTestMetadata("ClassB", "CLASS", 1));
        vectorStore.addEntry(generateTestEmbedding(2), "Test code snippet 3", createTestMetadata("ClassC", "METHOD", 2));

        // Then: El tamaño debe reflejar las entradas añadidas
        assertEquals(3, vectorStore.size(), "El VectorStore debe tener 3 entradas después de añadir 3");
    }

    @Test
    void testSearch_ReturnsResults() {
        // Given: Un VectorStore con entradas indexadas
        vectorStore.addEntry(generateTestEmbedding(0), "Sandbox execution code", createTestMetadata("DockerService", "CLASS", 7));
        vectorStore.addEntry(generateTestEmbedding(1), "String concatenation in loop", createTestMetadata("StringUtil", "METHOD", 10));
        vectorStore.addEntry(generateTestEmbedding(2), "Logger info statement", createTestMetadata("LoggerDemo", "METHOD", 5));

        // When: Realizamos una búsqueda
        float[] queryVector = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(queryVector, 1.0f); // Query vector simple

        List<VectorStore.SearchResult> results = vectorStore.search(queryVector, 2);

        // Then: Debe retornar resultados (máx. 2 por la petición)
        assertNotNull(results, "Los resultados no deben ser nulos");
        assertTrue(results.size() <= 2, "Máximo 2 resultados por petición topK=2");
        assertTrue(results.size() > 0, "Debe haber al menos un resultado");
    }

    @Test
    void testSearch_DefaultTopK() {
        // Given: VectorStore poblado
        vectorStore.addEntry(generateTestEmbedding(0), "Code A", createTestMetadata("ClassA", "CLASS", 1));
        vectorStore.addEntry(generateTestEmbedding(1), "Code B", createTestMetadata("ClassB", "METHOD", 1));

        // When: Búsqueda sin especificar topK (usa el default)
        float[] queryVector = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(queryVector, 1.0f);

        List<VectorStore.SearchResult> results = vectorStore.search(queryVector);

        // Then: Debe retornar el topK por defecto (10) o menos si hay Fewer entries
        assertNotNull(results, "Los resultados no deben ser nulos");
        assertTrue(results.size() <= VectorStore.SEARCH_TOP_K,
                "Los resultados deben ser como máximo " + VectorStore.SEARCH_TOP_K);
    }

    @Test
    void testSearch_EmptyStore() {
        // Given: VectorStore vacío
        float[] queryVector = new float[EmbeddingUtils.EMBEDDING_DIMENSION];

        // When: Búsqueda en almacén vacío
        List<VectorStore.SearchResult> results = vectorStore.search(queryVector, 5);

        // Then: Debe retornar lista vacía
        assertNotNull(results, "Los resultados no deben ser nulos");
        assertTrue(results.isEmpty(), "VectorStore vacío debe retornar lista de resultados vacía");
    }

    @Test
    void testSearch_QueryDimensionMismatch() {
        // Given: VectorStore poblado, query con dimensión incorrecta
        vectorStore.addEntry(generateTestEmbedding(0), "Test", createTestMetadata("Test", "CLASS", 1));

        // When: Búsqueda con vector de query de dimensión incorrecta
        float[] wrongDimensionVector = new float[100]; // Incorrecto: debería ser 384

        // Then: Debe lanzar IllegalArgumentException
        Executable executable = () -> vectorStore.search(wrongDimensionVector, 5);
        assertThrows(IllegalArgumentException.class, executable,
                "Búsqueda con dimensión incorrecta debe lanzar excepción");
    }

    @Test
    void testSearch_ResultsOrderedBySimilarity() {
        // Given: VectorStore con entradas que tienen distintas similitudes
        // Usamos embeddings que sabemos producirán diferentes similitudes
        vectorStore.addEntry(generateTestEmbedding(0), "Very relevant code", createTestMetadata("HighRelevance", "METHOD", 1));
        vectorStore.addEntry(generateTestEmbedding(1), "Less relevant code", createTestMetadata("LowRelevance", "METHOD", 1));
        vectorStore.addEntry(generateTestEmbedding(2), "Medium relevance code", createTestMetadata("MediumRelevance", "METHOD", 1));

        // When: Búsqueda
        float[] queryVector = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        Arrays.fill(queryVector, 1.0f);

        List<VectorStore.SearchResult> results = vectorStore.search(queryVector, 3);

        // Then: Los resultados deben estar ordenados de mayor a menor similitud
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getSimilarity() >= results.get(i + 1).getSimilarity(),
                    "Los resultados deben estar ordenados de mayor a menor similitud");
        }
    }

    @Test
    void testRemoveByMetadata() {
        // Given: Una entrada en el VectorStore
        VectorStore.EntryMetadata metadata = createTestMetadata("ClassToRemove", "CLASS", 3);
        vectorStore.addEntry(generateTestEmbedding(0), "Code to remove", metadata);

        assertEquals(1, vectorStore.size(), "Debe haber 1 entrada antes de remover");

        // When: Eliminamos por metadatos
        boolean removed = vectorStore.removeByMetadata(metadata);

        // Then: La eliminación debió ser exitosa
        assertTrue(removed, "La eliminación por metadatos debe retornar true");
        assertEquals(0, vectorStore.size(), "Debe haber 0 entradas después de remover");
    }

    @Test
    void testRemoveByMetadata_NotFound() {
        // Given: Metadatos que no existen en el almacén
        VectorStore.EntryMetadata nonExistent = createTestMetadata("NonExistent", "CLASS", 99);

        // When: Intentamos removerlo
        boolean removed = vectorStore.removeByMetadata(nonExistent);

        // Then: Debe retornar false (no encontrado)
        assertFalse(removed, "Eliminar metadatos inexistentes debe retornar false");
        // El tamaño debería permanecer unchanged
        assertEquals(0, vectorStore.size(), "El tamaño debe permanecer unchanged");
    }

    @Test
    void testClear() {
        // Given: VectorStore con entradas
        vectorStore.addEntry(generateTestEmbedding(0), "Code 1", createTestMetadata("ClassA", "CLASS", 1));
        vectorStore.addEntry(generateTestEmbedding(1), "Code 2", createTestMetadata("ClassB", "METHOD", 1));
        vectorStore.addEntry(generateTestEmbedding(2), "Code 3", createTestMetadata("ClassC", "FIELD", 1));

        assertEquals(3, vectorStore.size(), "Debe haber 3 entradas antes de clear");

        // When: Limpio el almacén
        vectorStore.clear();

        // Then: Debe estar vacío
        assertEquals(0, vectorStore.size(), "Después de clear, el tamaño debe ser 0");
    }

    @Test
    void testContains() {
        // Given: VectorStore vacío
        VectorStore.EntryMetadata metadata = createTestMetadata("TestClass", "CLASS", 1);

        // When: Verificamos que no contiene (está vacío)
        boolean contains = vectorStore.contains(metadata);

        // Then: Debe retornar false
        assertFalse(contains, "VectorStore vacío no debe contener la entrada");

        // After: Añadimos y verificamos que sí contiene
        vectorStore.addEntry(generateTestEmbedding(0), "Test code", metadata);
        boolean containsAfter = vectorStore.contains(metadata);
        assertTrue(containsAfter, "Después de añadir, debe contener la entrada");
    }

    @Test
    void testGetEntryMetadata_ThroughSearch() {
        // Given: Una entrada indexada
        vectorStore.addEntry(generateTestEmbedding(0), "Searchable code", createTestMetadata("SearchClass", "CLASS", 5));

        // When: Realizamos una búsqueda
        float[] queryVector = new float[EmbeddingUtils.EMBERDimension_DIMENSION];
        Arrays.fill(queryVector, 1.0f);
        List<VectorStore.SearchResult> results = vectorStore.search(queryVector, 1);

        // Then: El resultado debe contener metadatos accesibles
        assertNotNull(results, "Los resultados no deben ser nulos");
        assertEquals(1, results.size(), "Debe haber 1 resultado");
        VectorStore.SearchResult result = results.get(0);

        // Verificar que los metadatos están poblados
        assertNotNull(result.getEntry(), "El resultado debe tener una entrada");
        assertNotNull(result.getEntry().getMetadata(), "La entrada debe tener metadatos");
        assertEquals("SearchClass", result.getEntry().getMetadata().getElementName(),
                "El nombre del elemento debe coincidir");
        assertEquals("CLASS", result.getEntry().getMetadata().getElementType(),
                "El tipo debe ser CLASS");
        assertEquals(5, result.getEntry().getMetadata().getPhase(),
                "La fase debe ser 5");
    }

    /**
     * Crea un embedding de prueba con valores deterministas basados en el índice.
     *
     * @param index Índice usado para generar valores distintos
     * @return Vector de embedding de 384 dimensiones con valores deterministas
     */
    private float[] generateTestEmbedding(int index) {
        float[] embedding = new float[EmbeddingUtils.EMBEDDING_DIMENSION];
        // Llenar con valores basados en el índice para tener diferenciación
        Arrays.fill(embedding, index * 0.1f);
        // Modificar algunas posiciones para diferenciación
        embedding[index % EmbeddingUtils.EMBEDDING_DIMENSION] = (index + 1) * 0.1f;
        embedding[(index + 50) % EmbeddingUtils.EMBEDDING_DIMENSION] = (index + 10) * 0.1f;
        return embedding;
    }

    /**
     * Crea metadatos de prueba para VectorStore.
     *
     * @param elementName Nombre del elemento (clase, método, etc.)
     * @param elementType Tipo de elemento (CLASS, METHOD, FIELD, etc.)
     * @param phase Fase del proyecto
     * @return EntryMetadata configurado para tests
     */
    private VectorStore.EntryMetadata createTestMetadata(String elementName, String elementType, int phase) {
        VectorStore.EntryMetadata metadata = new VectorStore.EntryMetadata();
        // Usar campo accesible o constructor
        try {
            // Intentar por constructor
            java.lang.reflect.Constructor<VectorStore.EntryMetadata> constructor =
                    VectorStore.EntryMetadata.class.getDeclaredConstructor(String.class, int.class, int.class,
                            String.class, String.class, int.class);
            constructor.setAccessible(true);
            return constructor.newInstance("sourceFile.java", 1, 100, elementName, elementType, phase);
        } catch (Exception e) {
            // Fallback: usar el constructor por defecto y establecer campos públicamente
            VectorStore.EntryMetadata m = new VectorStore.EntryMetadata();
            m.sourceFile = "sourceFile.java";
            m.lineStart = 1;
            m.lineEnd = 100;
            m.elementName = elementName;
            m.elementType = elementType;
            m.phase = phase;
            return m;
        }
    }
}