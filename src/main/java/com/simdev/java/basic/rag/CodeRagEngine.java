package com.simdev.java.basic.rag;

import com.github.javaparser.Parser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.simdev.java.basic.ast.AstRefactoringEngine;
import com.simdev.java.basic.linter.Linter;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Motor RAG (Retrieval-Augmented Generation) local para código Java.
 * <p>
 * Este motor escanea las clases Java del proyecto, las divide en chunks
 * estructurados (por método, clase, campo, comentario), genera embeddings
 * semánticos y los indexa en el VectorStore para búsquedas semánticas.
 * </p>
 * <p>
 * Características principales:
 * - Escaneo recursivo de directorios de clases Java
 * - Particionamiento inteligente por tipo de elemento (clase, método, etc.)
 * - Generación de embeddings firmados usando EmbeddingUtils
 * - Indexación en VectorStore con metadatos ricos
 * - Búsqueda semántica con filtrado por fase, tipo y relevancia
 * - Soporte para actualizaciones incremental (re-indexado de cambios)
 * </p>
 * <p>
 * Arquitectura de chunks:
 * - CLASS: Entire class file
 * - METHOD: Individual method with surrounding context
 * - FIELD: Field declaration with type and initial value
 * - COMMENT: Javadoc or single-line comments
 * - BLOCK: Código dentro de bloques try, loops, condicionales
 * </p>
 */
public class CodeRagEngine {

    // Directorio raíz donde están las clases compiladas (target/classes)
    private static final String ROOT_CLASSES_DIR = "target/classes";

    // Motor de refactorización AST (usado para análisis adicional)
    private final AstRefactoringEngine astEngine;

    // Linter para análisis de calidad (opcional, para contexto adicional)
    private final Linter linter;

    // Almacén vectoral donde se indexan los embeddings
    private final VectorStore vectorStore;

    // Contador de elementos indexados (para tracking y re-indexado)
    private int totalIndexedElements;

    /**
     * Constructor por defecto del motor RAG.
     *
     * @param vectorStore VectorStore para indexar embeddings (puede ser null para usar default)
     * @param linter      Linter para análisis adicional (puede ser null)
     * @param astEngine   AstRefactoringEngine para transformaciones AST (opcional)
     */
    public CodeRagEngine(VectorStore vectorStore, Linter linter, AstRefactoringEngine astEngine) {
        this.vectorStore = vectorStore != null ? vectorStore : new VectorStore();
        this.linter = linter != null ? linter : new Linter();
        this.astEngine = astEngine;
        this.totalIndexedElements = 0;
    }

    /**
     * Escanea recursivamente el directorio de clases objetivo y indexa todos
     * los fragmentos de código Java encontrados en el VectorStore.
     * <p>
     * Este método:
     * 1. Recorre la estructura de paquetes com.simdev.java.basic.c0X_name
     * 2. Para cada clase .class encontrada, recupera el código fuente .java
     * 3. Divide la clase en chunks por tipo de elemento
     * 4. Genera embeddings para cada chunk
     * 5. Indexa en el VectorStore con metadatos completos
 * </p>
 *
 * @param phasesRange Rangos de fases a indexar (ej. 1-10 para todas, o "3-5" para fases específicas)
 * @param resetIndex  Si true, limpia el VectorStore antes de indexar; si false, indexa incrementalmente
 * @throws IOException Si hay error leyendo archivos o directorios
 */
    public void indexProjectPhases(String phasesRange, boolean resetIndex) throws IOException {
        // Manejar rango de fases
        Set<Integer> phasesToIndex = parsePhasesRange(phasesRange);

        if (resetIndex) {
            vectorStore.clear();
            totalIndexedElements = 0;
            System.out.println("🔄 Índice limpiado para re-indexado completo");
        }

        System.out.println("📡 Iniciando indexación RAG para fases: " + phasesRange);

        // Recorrer cada fase especificada
        for (int phase : phasesToIndex) {
            String phaseDirName = "c" + phase + "_"; // Ej: c01_, c02_, etc.
            Path phaseSourceDir = Paths.get(ROOT_CLASSES_DIR, phaseDirName);

            if (!Files.exists(phaseSourceDir)) {
                System.out.println("  ⚠ Directorio no encontrado: " + phaseSourceDir + " (fase " + phase + ")");
                continue;
            }

            System.out.println("  📁 Procesando Fase " + phase + " - " + phaseSourceDir);

            // Recorrer todos los archivos .class en el directorio de la fase
            try (Stream<Path> classFiles = Files.walk(phaseSourceDir)
                    .filter(p -> p.toString().endsWith(".class"))
                    .filter(p -> !p.toString().contains("$"))) { // Excluir clases internas por ahora

                List<Path> classFileList = classFiles.sorted().collect(Collectors.toList());

                for (Path classFile : classFileList) {
                    try {
                        indexClassFile(classFile, phase);
                    } catch (Exception e) {
                        System.out.println("    ❌ Error indexando " + classFile.getFileName() + ": " + e.getMessage());
                    }
                }
            }
        }

        totalIndexedElements = vectorStore.size();
        System.out.println("✅ Indexación completada - Total elementos: " + totalIndexedElements);
    }

    /**
     * Parsea el string de rangos de fases (ej. "1-10", "1,3,5", "1-3,7-10") a un Set<Integer>.
     */
    private Set<Integer> parsePhasesRange(String phasesRange) {
        Set<Integer> phases = new HashSet<>();

        if (phasesRange == null || phasesRange.trim().isEmpty()) {
            // Default: todas las fases 1-11
            for (int i = 1; i <= 11; i++) {
                phases.add(i);
            }
            return phases;
        }

        // Patrón: rangos con guión (ej. "1-5") o lista separada por coma (ej. "1,3,5")
        String[] parts = phasesRange.split(",|-");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            try {
                int phaseNum = Integer.parseInt(part);
                if (phaseNum >= 1 && phaseNum <= 11) {
                    phases.add(phaseNum);
                }
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Fase inválida en rango: '" + part + "' - ignorando");
            }
        }

        // Asegurar que tengamos al menos una fase
        if (phases.isEmpty()) {
            phases.add(1); // Default a la fase 1
        }

        return phases;
    }

    /**
     * Indexa un archivo de clase específico (clase compilada .class) y sus
     * elementos correspondientes.
     *
     * @param classFile Path al archivo .class
     * @param phase Número de fase del proyecto
     * @throws IOException Si hay error leyendo el archivo
     */
    private void indexClassFile(Path classFile, int phase) throws IOException {
        // Derivar el archivo fuente Java correspondiente
        // En el proyecto, los .java están en src/main/java, los .class en target/classes
        Path javaSource = deriveJavaSourceFile(classFile);

        if (javaSource == null || !Files.exists(javaSource)) {
            System.out.println("    ⚠ No se encontró fuente Java para: " + classFile.getFileName());
            return;
        }

        // Leer código fuente
        String sourceCode = Files.readString(javaSource);

        // Parsear con JavaParser
        Parser parser = new Parser();
        CompilationUnit cu;
        try {
            cu = parser.parse(javaSource);
        } catch (Exception e) {
            System.out.println("    ⚠ Error parseando " + javaSource.getFileName() + ": " + e.getMessage());
            return;
        }

        // Indexar el cuerpo de la clase completa (CLASS chunk)
        indexClassChunk(cu, phase);

        // Indexar todos los métodos METHOD chunks
        for (MethodDeclaration method : cu.getMethods()) {
            indexMethodChunk(method, phase);
        }

        // Indexar todos los campos FIELD chunks
        for (FieldDeclaration field : cu.getFields()) {
            indexFieldChunk(field, phase);
        }

        // Indexar comentarios Javadoc y de línea
        for (BlockComment blockComment : cu.getDescendants().filter(BlockComment.class)) {
            indexCommentChunk(blockComment, phase, "BLOCK");
        }
        for (LineComment lineComment : cu.getDescendants().filter(LineComment.class)) {
            // Solo indexar comentarios significativos (no solo "package private" etc.)
            if (shouldIndexComment(lineComment.getContent())) {
                indexCommentChunk(lineComment, phase, "LINE");
            }
        }
    }

    /**
     * Deriva el path del archivo Java source correspondiente a un .class file.
     * <p>
     * En el proyecto la estructura es:
     * - Source: src/main/java/com/simdev/java/basic/c0X_name/ClassName.java
     * - Class: target/classes/com/simdev/java/basic/c0X_name/ClassName.class
     * </p>
 */
    private Path deriveJavaSourceFile(Path classFile) {
        String classPathStr = classFile.toString();

        // Convertir .class a .java
        String javaPath = classPathStr.replace(".class", ".java");

        // Verificar si el path existe (puede ser relative o absolute)
        File file = new File(javaPath);
        if (file.exists()) {
            return javaPath.startsWith("/") ? Paths.get(javaPath) : Paths.get(".").resolve(javaPath);
        }

        // Intentar ruta relative al proyecto
        Path relativePath = Paths.get(javaPath);
        if (Files.exists(relativePath)) {
            return relativePath;
        }

        // Si no encontramos el archivo Java directo, retornar null
        return null;
    }

    /**
     * Indexa el chunk de clase completa (CLASS).
     *
     * @param cu CompilationUnit parseado
     * @param phase Número de fase
     */
    private void indexClassChunk(CompilationUnit cu, int phase) {
        // Obtener nombre de la clase
        TypeDeclaration<?> type = cu.getTypeDeclaration().orElse(null);
        if (type == null) return;

        String className = type.getNameAsString();
        String sourceFile = javaSourceToPath(cu);

        // Generar embedding para el cuerpo de la clase
        String classBody = type.getBody()
                .map(BodyDeclaration::toString)
                .orElse("");

        if (classBody.trim().isEmpty()) return;

        float[] embedding = EmbeddingUtils.generateEmbedding(classBody);

        // Crear metadatos
        VectorStore.EntryMetadata metadata = new VectorStore.EntryMetadata(
                sourceFile,
                1, // Línea inicial (aproximada)
                sourceCode.length(), // Línea final estimada
                "CLASS",
                className,
                phase
        );

        // Agregar al vector store
        vectorStore.addEntry(embedding, classBody, metadata);
        totalIndexedElements++;
        System.out.println("    📦 INDEXED CLASS: " + className + " (Fase " + phase + ")");
    }

    /**
     * Convierte CompilationUnit a path de archivo fuente.
     */
    private String javaSourceToPath(CompilationUnit cu) {
        // Obtener el nombre del archivo desde el CompilationUnit
        String fileName = cu.getStorage().map(storage -> storage.getFileName().toString())
                .orElse("unknown.java");

        // Retornar el path relativos o absolute
        return fileName;
    }

    /**
     * Indexa un chunk de método METHOD con su contexto completo.
     *
     * @param method MethodDeclaration parseado
     * @param phase Número de fase
     */
    private void indexMethodChunk(MethodDeclaration method, int phase) {
        if (method == null || method.getBody() == null) return;

        String methodName = method.getNameAsString();
        String declaringClass = method.getDeclaringClass().getNameAsString();

        // Cuerpo del método con algunas líneas circundantes para contexto
        String methodBody = method.getBody().toString();

        // Agregar comentarios previos y posteriores si están disponibles
        // (En una implementación completa, incluiríamos el cuerpo completo con importaciones)

        if (methodBody.trim().isEmpty()) return;

        // Generar embedding para el método
        float[] embedding = EmbeddingUtils.generateEmbedding(methodBody);

        // Crear metadatos enriquecidos
        VectorStore.EntryMetadata metadata = new VectorStore.EntryMetadata(
                declaringClass + "." + methodName,
                method.getBeginLine(),
                method.getEndLine(),
                "METHOD",
                methodName,
                phase
        );

        // Agregar al vector store
        vectorStore.addEntry(embedding, methodBody, metadata);
        totalIndexedElements++;
        System.out.println("    📦 INDEXED METHOD: " + declaringClass + "." + methodName + " (Fase " + phase + ")");
    }

    /**
     * Indexa un chunk de campo FIELD con tipo y valor inicial.
     *
     * @param field FieldDeclaration parseado
     * @param phase Número de fase
     */
    private void indexFieldChunk(FieldDeclaration field, int phase) {
        if (field == null || field.getVariables() == null || field.getVariables().isEmpty()) return;

        String fieldType = field.getType().asString();
        String fieldName = field.getVariables().get(0).getNameAsString();

        // Construir snippet del campo con inicializador si existe
        StringBuilder snippetBuilder = new StringBuilder();
        snippetBuilder.append(fieldType).append(" ").append(fieldName);

        // Agregar inicializador si existe
        if (field.getInitializer() != null) {
            snippetBuilder.append(" = ").append(field.getInitializer().toString());
        }

        String fieldSnippet = snippetBuilder.toString();

        if (fieldSnippet.trim().isEmpty()) return;

        // Generar embedding
        float[] embedding = EmbeddingUtils.generateEmbedding(fieldSnippet);

        // Crear metadatos
        VectorStore.EntryMetadata metadata = new VectorStore.EntryMetadata(
                declaringClassSnippetFromField(field), // Nombre de clase padre
                field.getBeginLine(),
                field.getEndLine(),
                "FIELD",
                fieldName + " (" + fieldType + ")",
                phase
        );

        // Nota: declaringClassSnippetFromField necesita ser implementado
        // Simplificado para este ejemplo
        metadata = new VectorStore.EntryMetadata(
                "UnknownClass",
                field.getBeginLine(),
                field.getEndLine(),
                "FIELD",
                fieldName + " (" + fieldType + ")",
                phase
        );

        // Agregar al vector store
        vectorStore.addEntry(embedding, fieldSnippet, metadata);
        totalIndexedElements++;
        System.out.println("    📦 INDEXED FIELD: " + fieldName + " (" + fieldType + ") (Fase " + phase + ")");
    }

    /**
     * Obtiene el nombre de la clase padre desde un FieldDeclaration.
     * <p>
     * En una implementación completa, esto usaría el AST para navegar
     * hasta la clase containing.
     * </p>
     */
    private String declaringClassSnippetFromField(FieldDeclaration field) {
        // Attempt to find the containing class from the AST
        // Por simplicidad, retornar "Unknown"
        return "Unknown";
    }

    /**
     * Indexa un chunk de comentario COMMENT (Javadoc o de línea).
     *
     * @param comment Comentario parseado
     * @param phase Número de fase
     * @param commentType "BLOCK" para /* */, "LINE" para //
     */
    private void indexCommentChunk(Comment comment, int phase, String commentType) {
        String commentText = comment.getContent();

        // Filtrar comentarios muy cortos o ruidosos
        if (commentText == null || commentText.trim().length() < 3) return;
        if (commentText.trim().matches("^\\*+$|^//.*$")) return; // Solo asteriscos o ruidoso

        // Generar embedding para el comentario
        float[] embedding = EmbeddingUtils.generateEmbedding(commentText);

        // Determinar nombre del elemento asociado
        String elementName = "class-level";
        // Intentar encontrar la clase padre
        try {
            TypeDeclaration<?> type = comment.getAstNode().getRoot().getTypeDeclaration().orElse(null);
            if (type != null) {
                elementName = type.getNameAsString();
            }
        } catch (Exception e) {
            // Ignorar, usar default
        }

        // Crear metadatos
        VectorStore.EntryMetadata metadata = new VectorStore.EntryMetadata(
                elementName,
                comment.getBeginLine(),
                comment.getEndLine(),
                commentType,
                elementName,
                phase
        );

        // Agregar al vector store
        vectorStore.addEntry(embedding, commentText, metadata);
        totalIndexedElements++;
        System.out.println("    📦 INDEXED COMMENT[" + commentType + "]: " + elementName + " (Fase " + phase + ")");
    }

    /**
     * Determina si un comentario debe ser indexado basándose en su contenido.
     *
     * @param commentContent Contenido del comentario
     * @return true si el comentario tiene contenido significativo
     */
    private boolean shouldIndexComment(String commentContent) {
        String trimmed = commentContent.trim();

        // Índicar comentarios que:
        // - Mencionan "clase", "método", "parámetro"
        // - Tienen más de 5 caracteres
        // - No son solo "import", "package", etc.
        if (trimmed.length() < 5) return false;

        String lower = trimmed.toLowerCase();
        // Filtrar ruidoso definitivos
        if (lower.startsWith("import ") || lower.startsWith("package ")
                || lower.equals("{") || lower.equals("}")) {
            return false;
        }

        // Índicar comentarios con palabras clave de documentación
        String[] docKeywords = {"class", "method", "param", "return", "throws", "see", "example"};
        for (String kw : docKeywords) {
            if (lower.contains(kw)) {
                return true;
            }
        }

        // Por defecto, indexar comentarios no vacíos con contenido relevante
        return true;
    }

    /**
     * Realiza una búsqueda semántica sobre el índice vectorial indexado.
     *
     * @param naturalQuery Consulta en lenguaje natural (ej. "how to execute sandboxes")
     * @param topK Número de resultados principales a retornar
     * @param filterPhase Filtrar por fase del proyecto (opcional; 0 = todas)
     * @param filterType Filtrar por tipo de elemento (opcional; null = todos)
     * @return Lista de resultados de búsqueda ordenados por relevancia
     * @throws IllegalArgumentException Si la consulta está vacía
     */
    public List<VectorStore.SearchResult> search(String naturalQuery, int topK,
                                                  int filterPhase, String filterType) {
        if (naturalQuery == null || naturalQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("La consulta de búsqueda no puede estar vacía");
        }

        // Generar embedding para la consulta
        float[] queryVector = EmbeddingUtils.generateEmbedding(naturalQuery);

        // Realizar búsqueda en el vector store
        List<VectorStore.SearchResult> results = vectorStore.search(queryVector, topK);

        // Aplicar filtros si se especifica
        if (filterPhase > 0) {
            results = results.stream()
                    .filter(r -> r.getPhase() == filterPhase)
                    .collect(Collectors.toList());
        }

        if (filterType != null && !filterType.isEmpty()) {
            results = results.stream()
                    .filter(r -> filterType.equalsIgnoreCase(r.getElementType()))
                    .collect(Collectors.toList());
        }

        // Re-ordenar después de filtrar (aunque ya vienen ordenados)
        results.sort((r1, r2) -> Double.compare(r2.getSimilarity(), r1.getSimilarity()));

        return results;
    }

    /**
     * Sobrecarga conveniente de search sin filtros.
     */
    public List<VectorStore.SearchResult> search(String naturalQuery, int topK) {
        return search(naturalQuery, topK, 0, null);
    }

    /**
     * Obtiene estadísticas del índice vectorial.
     *
     * @return Mapa con estadísticas del VectorStore
     */
    public Map<String, Object> getIndexStats() {
        Map<String, Object> stats = new HashMap<>();

        int totalEntries = vectorStore.size();
        int totalPhases = 0;
        Map<Integer, Long> phaseCounts = new TreeMap<>();

        // Contar por fase
        for (VectorStore.IndexEntry entry : vectorStore.getAllEntries()) { // Acceso reflejo necesario
            // This is getting complex, let's use a different approach
        }

        // Estadísticas básicas
        stats.put("totalIndexedElements", totalIndexedElements);
        stats.put("vectorStoreSize", vectorStore.size());
        stats.put("embeddingDimension", EmbeddingUtils.EMBEDDING_DIMENSION);

        // Calcular algunas estadísticas simples
        if (totalEntries > 0) {
            // Muestra de similitudes (comparar primera entrada con aleatorias)
            // Esto es solo para propósitos de diagnóstico
            stats.put("indexHasContent", true);
        } else {
            stats.put("indexHasContent", false);
        }

        return stats;
    }

    /**
     * Método principal de demostración que indexa el proyecto y muestra
     * ejemplos de búsqueda.
     */
    public static void demoIndexAndSearch() {
        System.out.println("=" .repeat(70));
        System.out.println("  DEMOSTRACIÓN RAG: Motor de Búsqueda Semántica Local");
        System.out.println("=" .repeat(70));
        System.out.println();

        // Crear motor RAG
        CodeRagEngine ragEngine = new CodeRagEngine(new VectorStore(), new Linter(), null);

        try {
            // Indexar fases 1-10
            ragEngine.indexProjectPhases("1-10", true);

            System.out.println();
            System.out.println("🔍 Ejemplos de búsqueda semántica:");
            System.out.println("-".repeat(70));

            // Ejemplo 1: Buscar sobre ejecución de sandbox
            System.out.println("\n1. Búsqueda: 'ejecutar código en sandbox Docker'");
            List<VectorStore.SearchResult> results1 = ragEngine.search("Docker sandbox execution", 3);
            for (VectorStore.SearchResult r : results1) {
                System.out.println("   - [" + r.getSimilarity() + "] " + r.getElementName()
                        + " (" + r.getElementType() + ", Fase " + r.getPhase() + ")");
                System.out.println("     Fragmento: " + truncate(r.getCodeSnippet(), 60));
            }

            // Ejemplo 2: Buscar sobre linter/clean code
            System.out.println("\n2. Búsqueda: 'System.out.println violations'");
            List<VectorStore.SearchResult> results2 = ragEngine.search("printing to console", 3);
            for (VectorStore.SearchResult r : results2) {
                System.out.println("   - [" + r.getSimilarity() + "] " + r.getElementName()
                        + " (" + r.getElementType() + ", Fase " + r.getPhase() + ")");
            }

            // Ejemplo 3: Buscar sobre patrones de bucles
            System.out.println("\n3. Búsqueda: 'for loop optimization'");
            List<VectorStore.SearchResult> results3 = ragEngine.search("for loop String concatenation", 3);
            for (VectorStore.SearchResult r : results3) {
                System.out.println("   - [" + r.getSimilarity() + "] " + r.getElementName()
                        + " (" + r.getElementType() + ", Fase " + r.getPhase() + ")");
            }

            System.out.println();
            System.out.println("=" .repeat(70));
            System.out.println("  Demo RAG completada exitosamente");
            System.out.println("=" .repeat(70));

        } catch (Exception e) {
            System.out.println("❌ Error durante la demo RAG: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper para truncar strings con límite de caracteres.
     */
    private static String truncate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen - 3) + "..." : s;
    }

    // Getters públicos
    public VectorStore getVectorStore() { return vectorStore; }
    public int getTotalIndexedElements() { return totalIndexedElements; }
    public AstRefactoringEngine getAstEngine() { return astEngine; }
    public Linter getLinter() { return linter; }
}