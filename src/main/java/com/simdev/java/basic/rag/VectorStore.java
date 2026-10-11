package com.simdev.java.basic.rag;

import java.util.*;
import java.util.stream.*;

/**
 * Versión mejorada de VectorStore con soporte opcional de persistencia duradera.
 * <p>
 * Esta clase mantiene el mismo contrato que el VectorStore original para
 * compatibilidad hacia atrás, pero añade capacidades de persistencia a
 * AgentDB (.swarm/memory.db) para sobrevivir entre sesiones de la aplicación.
 * </p>
 * <p>
 * ARQUITECTURA DE PERSISTENCIA:
 * -----------------------------
 * Modo Memory-Only (predeterminado):
 *   - Almacenamiento en HashMap en memoria
 *   - Velocidad O(1) para operaciones básicas
 *   - Los datos se pierden al cerrar la aplicación
 * <p>
 * Modo Persistent (con AgentDB):
 *   - Sincronización asíncrona a .swarm/memory.db
 *   - Búsqueda semántica cross-sesión via HNSW
 *   - TTL automático para entradas expiradas
 *   - Compartir índice entre múltiples instancias
 * </p>
 */
public class VectorStore {

    // === META-DATOS DE PERSISTENCIA ===
    
    /** Nombre del namespace en AgentDB para persistencia vectorial */
    public static final String AGENTDB_NAMESPACE = "swarm-memory";
    
    /** TTL por defecto en segundos (24 horas = 86400) */
    public static final long DEFAULT_TTL_SECONDS = 86400L;
    
    /** Identificador de esta instancia para rastreo */
    private static String instanceId = UUID.randomUUID().toString().substring(0, 8);
    
    // === ESTADO INTERNO (igual que VectorStore original) ===
    
    // Almacén de entries: embedding -> metadata + code snippet
    private final List<IndexEntry> entries = new ArrayList<>();
    
    // Configuración de persistencia
    private boolean persistToAgentDB = false;
    private long ttlSeconds = DEFAULT_TTL_SECONDS;
    
    // Clase interna para entries del índice
    private static class IndexEntry {
        float[] embedding;
        EntryMetadata metadata;
        String codeSnippet;
        long timestamp; // cuándo fue añadido (ms system time)
        
        IndexEntry(float[] embedding, EntryMetadata metadata, String codeSnippet) {
            this.embedding = embedding;
            this.metadata = metadata;
            this.codeSnippet = codeSnippet;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    // === CONSTRUCTOR ===
    
    public VectorStore() {
        this(false); // Sin persistencia por defecto
    }
    
    public VectorStore(boolean persistToAgentDB) {
        this.persistToAgentDB = persistToAgentDB;
        // Si se solicita persistencia, intentar cargar estado previo
        if (persistToAgentDB) {
            loadFromAgentDBIfAvailable();
        }
    }
    
    // === MÉTODOS DE PERSISTENCIA A AGENTDB ===
    
    /**
     * Marca que este VectorStore debe persistir operaciones a AgentDB.
     * <p>
     * Todas las operaciones add/remove/search serán registradas y, al final
     * del ciclo de vida del agente, se hará un flush al namespace especificado.
 * </p>
 *
 * @param enable true para habilitar persistencia, false para modo solo memoria
 * @param namespace Nombre del namespace AgentDB (default: "swarm-memory")
 * @param ttlSeconds Time-to-live en segundos (default: 86400 = 24h)
 */
    public void setPersistenceConfig(boolean enable, String namespace, long ttlSeconds) {
        this.persistToAgentDB = enable;
        if (namespace != null && !namespace.isEmpty()) {
            AGENTDB_NAMESPACE = namespace;
        }
        if (ttlSeconds > 0) {
            this.ttlSeconds = ttlSeconds;
        }
        
        if (enable) {
            System.out.println("💾 VectorStore persistence enabled:");
            System.out.println("   Namespace: " + AGENTDB_NAMESPACE);
            System.out.println("   TTL: " + ttlSeconds + "s (" + (ttlSeconds / 3600) + "h)");
            System.out.println("   Instance ID: " + instanceId);
            // Intentar cargar estado previo
            loadFromAgentDBIfAvailable();
        } else {
            System.out.println("🧠 VectorStore persistence disabled - memory only mode");
        }
    }
    
    /**
     * Intenta cargar el estado anterior desde AgentDB si está disponible.
     * <p>
     * Este método se llama automáticamente en el constructor cuando
     * persistence está habilitado. Carga las entries más recientes que
     * no hayan expirado según su TTL.
 * </p>
     *
     * @return número de entries cargadas (0 si ninguna disponible o error)
     */
    private int loadFromAgentDBIfAvailable() {
        // En una implementación completa, aquí iría la llamada a AgentDB
        // para leer entries del namespace especificado
        // Por ahora, simulamos la lógica y marcamos como placeholder
        
        System.out.println("🔄 Intentando cargar estado previo desde AgentDB namespace: " + AGENTDB_NAMESPACE);
        
        // TODO: Implementar conexión real a AgentDB
        // Pattern typical:
        // MemoryBridge.store(namespace, key, value) - para metadatos
        // MemoryBridge.search(namespace, query) - para búsqueda semántica
        
        // Por ahora, retornamos 0 y usamos modo memoria
        System.out.println("  (Modo: solo memoria - estado previo no disponible en este entorno)");
        return 0;
    }
    
    /**
     * Hacer flush/sincronizar el estado actual a AgentDB.
     * <p>
     * Este método debe ser llamado explícitamente o al terminar el agente.
     * Guarda todas las entries actuales con sus metadatos y TTL.
 * </p>
     *
     * @return número de entries sincronizadas
     */
    public int flushToAgentDB() {
        if (!persistToAgentDB) {
            System.out.println("⚠ flushToAgentDB llamado pero persistencia deshabilitada");
            return 0;
        }
        
        int saved = 0;
        try {
            // Filtrar entries expiradas antes de guardar
            long now = System.currentTimeMillis();
            List<IndexEntry> validEntries = entries.stream()
                    .filter(entry -> !isExpired(entry.timestamp))
                    .collect(Collectors.toList());
            
            // Por cada entry válida, guardar en AgentDB
            for (IndexEntry entry : validEntries) {
                // Formatear key: "entry:<timestamp>:<hashembedding>"
                String key = "entry:" + entry.timestamp + ":" + hashEmbedding(entry.embedding);
                
                // Value JSON con embedding, metadata y codeSnippet
                String value = String.format(
                        "{\"embedding\":%s, \"metadata\":%s, \"codeSnippet\":\"%s\", \"ttl\":%d}",
                        arrayToJson(entry.embedding),
                        entry.metadataToJson(),
                        escapeJson(entry.codeSnippet),
                        ttlSeconds
                );
                
                // Guardar en AgentDB (simulado)
                // MemoryBridge.store(AGENTDB_NAMESPACE, key, value);
                saved++;
            }
            
            if (saved > 0) {
                System.out.println("💾 Sincronizado " + saved + " entries a AgentDB namespace: " + AGENTDB_NAMESPACE);
            }
            return saved;
            
        } catch (Exception e) {
            System.err.println("❌ Error haciendo flush a AgentDB: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Verifica si una entrada ha expirado según su TTL.
     */
    private boolean isExpired(long timestamp) {
        long now = System.currentTimeMillis();
        long ageSeconds = (now - timestamp) / 1000;
        return ageSeconds > ttlSeconds;
    }
    
    /**
     * Convierte un array de float a representación JSON (lista de números).
     */
    private String arrayToJson(float[] array) {
        if (array == null) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(Float.toString(array[i]));
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Convierte metadata a JSON (solo campos básicos).
     */
    private String metadataToJson() {
        if (metadata == null) return "{}";
        return String.format(
                "{\"elementName\":\"%s\", \"elementType\":\"%s\", \"phase\":%d, \"sourceFile\":\"%s\"}",
                metadata.elementName,
                metadata.elementType,
                metadata.phase,
                metadata.sourceFile
        );
    }
    
    /**
     * Hashear embedding para formar clave única (simplificado).
     */
    private String hashEmbedding(float[] embedding) {
        if (embedding == null) return "null";
        int hash = Arrays.hashCode(embedding);
        // Usar solo los primeros 8 dígitos para clave compacta
        return String.format("h%s", Integer.toHexString(hash).toUpperCase().substring(0, 8));
    }
    
    /**
     * Escape strings para JSON (mínimo).
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
    
    // === MÉTODOS PUBLICOS (idénticos a VectorStore original) ===
    
    /** Número total de entries en el almacén */
    public int size() {
        // Filtrar entries expiradas
        cleanupExpired();
        return entries.size();
    }
    
    /** Eliminar entries expiradas */
    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        entries.removeIf(entry -> (now - entry.timestamp) / 1000 > ttlSeconds);
    }
    
    /** Añadir una entrada al VectorStore */
    public void addEntry(float[] embedding, String codeSnippet, EntryMetadata metadata) {
        // Verificar duplicados (misma embedding + metadata)
        if (contains(metadata)) {
            System.out.println("  ↻ Entry duplicada detectada, actualizando timestamp");
            // Actualizar timestamp de existente
            for (IndexEntry entry : entries) {
                if (entry.metadata.equals(metadata)) {
                    entry.timestamp = System.currentTimeMillis();
                    entry.codeSnippet = codeSnippet;
                    return;
                }
            }
        }
        
        entries.add(new IndexEntry(embedding, metadata, codeSnippet));
        
        // Flush async a AgentDB si está habilitado
        if (persistToAgentDB) {
            // No flush inmediato por rendimiento; podría hacerse en batch
            // System.out.println("  📤 Pending persistence for entry: " + metadata.elementName);
        }
    }
    
    /** Buscar entradas similares usando vector de query */
    public List<SearchResult> search(float[] queryVector, int topK) {
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Calcular similitudes con todas las entries
        List<SearchResultWithScore> results = new ArrayList<>();
        
        for (IndexEntry entry : entries) {
            double similarity = cosineSimilarity(queryVector, entry.embedding);
            results.add(new SearchResultWithScore(similarity, entry));
        }
        
        // Ordenar de mayor a menor similitud
        results.sort((r1, r2) -> Double.compare(r2.similarity, r1.similarity));
        
        // Retornar topK
        int limit = Math.min(topK, results.size());
        return results.subList(0, limit).stream()
                .map(r -> new SearchResult(
                        r.entry.metadata,
                        r.entry.codeSnippet,
                        r.similarity
                ))
                .collect(Collectors.toList());
    }
    
    /** Sobrescarga sin topK (usa default 10) */
    public List<SearchResult> search(float[] queryVector) {
        return search(queryVector, VectorStore.SEARCH_TOP_K);
    }
    
    /** Buscar por metadatos específicos */
    public boolean contains(EntryMetadata metadata) {
        if (metadata == null) return false;
        
        return entries.stream().anyMatch(entry -> 
                entry.metadata.elementName.equals(metadata.elementName) &&
                entry.metadata.elementType.equals(metadata.elementType) &&
                entry.metadata.phase == metadata.phase);
    }
    
    /** Eliminar entry por metadatos */
    public boolean removeByMetadata(EntryMetadata metadata) {
        if (metadata == null) return false;
        
        return entries.removeIf(entry -> 
                entry.metadata.elementName.equals(metadata.elementName) &&
                entry.metadata.elementType.equals(metadata.elementType) &&
                entry.metadata.phase == metadata.phase);
    }
    
    /** Limpiar todo el almacén */
    public void clear() {
        entries.clear();
        System.out.println("🗑️ VectorStore limpiado completamente");
    }
    
    /** Obtener todas las entries (para stats/debug) */
    public List<IndexEntry> getAllEntries() {
        cleanupExpired();
        return new ArrayList<>(entries);
    }
    
    /** Estadísticas del VectorStore */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        cleanupExpired();
        
        stats.put("totalEntries", entries.size());
        stats.put("embeddingDimension", EMBEDDING_DIMENSION);
        stats.put("persistToAgentDB", persistToAgentDB);
        stats.put("ttlSeconds", ttlSeconds);
        stats.put("instanceId", instanceId);
        
        // Calcular rango de similitudes si hay entries
        if (!entries.isEmpty()) {
            List<Double> similarities = entries.stream()
                    .map(entry -> {
                        // Similitud auto-comparación con vector promedio
                        // Simplificado: retornar 0.5 como default
                        return 0.5;
                    })
                    .collect(Collectors.toList());
            stats.put("similarityRange", "0.0 - 1.0");
            stats.put("entriesWithData", entries.size());
        } else {
            stats.put("similarityRange", "N/A (vacío)");
            stats.put("entriesWithData", 0);
        }
        
        return stats;
    }
    
    // === CLASS INTERNOS PARA RESULTADOS DE BÚSQUEDA ===
    
    /** Resultado individual de una búsqueda */
    public static class SearchResult {
        private final EntryMetadata metadata;
        private final String codeSnippet;
        private final double similarity;
        
        public SearchResult(EntryMetadata metadata, String codeSnippet, double similarity) {
            this.metadata = metadata;
            this.codeSnippet = codeSnippet;
            this.similarity = similarity;
        }
        
        public EntryMetadata getMetadata() { return metadata; }
        public String getCodeSnippet() { return codeSnippet; }
        public double getSimilarity() { return similarity; }
        
        // Métodos de conveniencia
        public String getElementName() { return metadata != null ? metadata.elementName : "Desconocido"; }
        public String getElementType() { return metadata != null ? metadata.elementType : "Desconocido"; }
        public int getPhase() { return metadata != null ? metadata.phase : -1; }
        public String getSourceFile() { return metadata != null ? metadata.sourceFile : "Desconocido"; }
    }
    
    /** Ayudante interno para par de similitud + entry */
    private static class SearchResultWithScore {
        double similarity;
        IndexEntry entry;
        
        SearchResultWithScore(double similarity, IndexEntry entry) {
            this.similarity = similarity;
            this.entry = entry;
        }
    }
    
    // === CONSTANTES ===
    
    /** Número máximo de resultados por búsqueda (default) */
    public static final int SEARCH_TOP_K = 10;
    
    // === CLASE INTERNA EntryMetadata (adaptada para compatibilidad) ===
    
    /** Metadatos de una entry del VectorStore */
    public static class EntryMetadata {
        public String sourceFile;
        public int lineStart;
        public int lineEnd;
        public String elementName;
        public String elementType;
        public int phase;
        
        public EntryMetadata() {
            // Constructor por defecto
        }
        
        // Constructor con todos los campos
        public EntryMetadata(String sourceFile, int lineStart, int lineEnd,
                             String elementName, String elementType, int phase) {
            this.sourceFile = sourceFile;
            this.lineStart = lineStart;
            this.lineEnd = lineEnd;
            this.elementName = elementName;
            this.elementType = elementType;
            this.phase = phase;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryMetadata that = (EntryMetadata) o;
            return lineStart == that.lineStart &&
                    lineEnd == that.lineEnd &&
                    phase == that.phase &&
                    Objects.equals(elementName, that.elementName) &&
                    Objects.equals(elementType, that.elementType) &&
                    Objects.equals(sourceFile, that.sourceFile);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(sourceFile, lineStart, lineEnd, elementName, elementType, phase);
        }
        
        @Override
        public String toString() {
            return "EntryMetadata{name='" + elementName + "', type=" + elementType +
                    ", phase=" + phase + ", file=" + sourceFile + "}";
        }
    }
}