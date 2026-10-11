package com.simdev.java.basic.rag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el Motor de Búsqueda Semántica RAG (Fase 11).
 * <p>
 * Expone endpoints para búsquedas semánticas, re-indexación y estadísticas
 * del índice vectorial local. Este controlador integra el {@link CodeRagEngine}
 * con la capa Spring Boot, permitiendo consultas desde la API REST o el Dashboard Web.
 * </p>
 * <p>
 * Endpoints disponibles:
 * <ul>
 *   <li>{@code GET /api/rag/search}: Búsqueda semántica con filtros opcionales</li>
 *   <li>{@code POST /api/rag/index}: Re-indexación forzada de fases del proyecto</li>
 *   <li>{@code GET /api/rag/stats}: Estadísticas actuales del índice</li>
 * </ul>
 * </p>
 * <p>
 * Parámetros de consulta soportados:
 * <ul>
 *   <li>{@code query}: Consulta en lenguaje natural (obligatorio para GET search)</li>
 *   <li>{@code topK}: Número de resultados principales (default: 5)</li>
 *   <li>{@code phaseFilter}: Filtrar por fase del proyecto (0 = todas, 1-11)</li>
 *   <li>{@code typeFilter}: Filtrar por tipo de elemento ("CLASS", "METHOD", "FIELD", "LINE", "BLOCK")</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/rag")
public class RagRestController {

    private final CodeRagEngine ragEngine;

    /**
     * Constructor que inyecta el motor RAG.
     *
     * @param ragEngine Instancia de CodeRagEngine (inyectada por Spring)
     */
    public RagRestController(CodeRagEngine ragEngine) {
        this.ragEngine = ragEngine;
    }

    /**
     * Realiza una búsqueda semántica sobre el índice vectorial indexado.
     * <p>
     * Utiliza el motor CodeRagEngine para convertir la consulta en lenguaje natural
     * a un vector de embedding y buscar los fragmentos de código más relevantes.
     * </p>
     *
     * @param query Consulta en lenguaje natural (ej. "Docker sandbox execution")
     * @param topK  Número de resultados principales a retornar (default: 5)
     * @param phaseFilter Filtrar por fase del proyecto (0 = todas las fases, 1-11 para fase específica)
     * @param typeFilter  Filtrar por tipo de elemento ("CLASS", "METHOD", "FIELD", "LINE", "BLOCK")
     *                    (null o vacío = todos los tipos)
     * @return ResponseEntity con lista de resultados de búsqueda con puntuaciones de relevancia
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam(name = "query", required = true) String query,
            @RequestParam(name = "topK", defaultValue = "5") int topK,
            @RequestParam(name = "phaseFilter", defaultValue = "0") int phaseFilter,
            @RequestParam(name = "typeFilter", required = false) String typeFilter) {

        // Ejecutar búsqueda en el motor RAG
        List<VectorStore.SearchResult> results;
        if (phaseFilter > 0 && typeFilter != null && !typeFilter.isEmpty()) {
            results = ragEngine.search(query, topK, phaseFilter, typeFilter);
        } else if (phaseFilter > 0) {
            results = ragEngine.search(query, topK, phaseFilter, null);
        } else if (typeFilter != null && !typeFilter.isEmpty()) {
            results = ragEngine.search(query, topK, 0, typeFilter);
        } else {
            results = ragEngine.search(query, topK);
        }

        // Mapear resultados a estructura JSON comprensible para la API
        List<Map<String, Object>> response = results.stream().map(result -> {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("elementName", result.getElementName());
            item.put("elementType", result.getElementType());
            item.put("phase", result.getPhase());
            item.put("sourceFile", result.getSourceFile());
            item.put("similarity", result.getSimilarity());
            item.put("codeSnippet", truncateCode(result.getCodeSnippet(), 200));
            item.put("lineStart", result.getLineStart());
            item.put("lineEnd", result.getLineEnd());
            return item;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Fuerza la re-indexación de las fases del proyecto en el VectorStore.
     * <p>
     * Limpia el índice actual y vuelve a indexar todas las clases Java
     * desde el directorio target/classes para las fases especificadas.
 * </p>
 *
 * @param range Rangos de fases a indexar (formato: "1-11", "1,3,5", "1-3,7-10", etc.)
 * @param reset Si true, limpia el índice actual antes de indexar; si false, indexa incrementalmente
 * @return ResponseEntity con mensaje de confirmación y número de elementos indexados
 */
    @PostMapping("/index")
    public ResponseEntity<Map<String, Object>> index(@RequestParam(name = "range", defaultValue = "1-11") String range,
                                                     @RequestParam(name = "reset", defaultValue = "true") boolean reset) {

        try {
            ragEngine.indexProjectPhases(range, reset);

            // Obtener estadísticas después de la indexación
            Map<String, Object> stats = ragEngine.getIndexStats();

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Indexación completada exitosamente");
            response.put("range", range);
            response.put("reset", reset);
            response.put("indexedElements", stats.get("totalIndexedElements"));
            response.put("vectorStoreSize", stats.get("vectorStoreSize"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("message", "Error durante la indexación: " + e.getMessage());
            errorResponse.put("range", range);
            errorResponse.put("reset", reset);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Retorna las estadísticas actuales del índice vectorial.
     * <p>
     * Proporciona un vistazo general al estado del índice RAG incluyendo:
 * - Número total de elementos indexados
 * - Tamaño actual del VectorStore
 * - Dimensión de los embeddings
 * - Indicador de contenido indexado
 * </p>
 *
 * @return ResponseEntity con un mapa de estadísticas del índice vectorial
 */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = ragEngine.getIndexStats();

        // Añadir información adicional útil
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("indexStats", stats);
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "RAG Semantic Search Engine - Phase 11");
        response.put("endpointsAvailable", List.of(
                "GET /api/rag/search?query=...&topK=5&phaseFilter=0&typeFilter=...",
                "POST /api/rag/index?range=1-11&reset=true",
                "GET /api/rag/stats"));

        return ResponseEntity.ok(response);
    }

    /**
     * Trunca un fragmento de código para incluirse en respuestas JSON.
     *
     * @param code Fragmento de código potencialmente largo
     * @param maxLength Longitud máxima permitida
     * @return Cadena truncada con "..." si excedía el límite
     */
    private String truncateCode(String code, int maxLength) {
        if (code == null) return "";
        if (code.length() <= maxLength) return code;
        return code.substring(0, maxLength - 3) + "...";
    }
}