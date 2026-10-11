package com.simdev.java.basic.rag;

import java.util.*;
import java.util.stream.*;

/**
 * Ejecutor e consultor RAG interactivo.
 * <p>
 * Proporciona una interfaz de línea de comandos para realizar búsquedas
 * semánticas en vivo sobre el índice vectorial indexado de código Java.
 * Puede operar en modo interactivo (bucle CLI) o modo batch (consultas únicas).
 * </p>
 * <p>
 * Modos de operación:
 * - Modo interactivo: Bucle continuo, acepta consultas del usuario hasta 'quit' o 'exit'
 * - Modo batch: Ejecuta una única consulta y sale
 * - Modo reporte: Genera reporte JSON de todas las búsquedas o estadísticas del índice
 * </p>
 * <p>
 * Comandos CLI soportados:
 * - `search <consulta>`: Buscar y mostrar top resultados
 * - `index`: Volver a indexar todo el proyecto
 * - `stats`: Mostrar estadísticas del índice
 * - `quit` / `exit`: Salir del modo interactivo
 * - `help`: Mostrar ayuda de comandos
 * </p>
 */
public class RagRunner {

    private final CodeRagEngine ragEngine;
    private final VectorStore vectorStore;

    // Mensaje de bienvenida
    private static final String WELCOME_MSG = """
            === Motor de Búsqueda Semántica RAG v1.0 ===
            
            Comandos disponibles:
              search <consulta>  - Buscar código semánticamente
              index              - Volver a indexar proyecto
              stats              - Mostrar estadísticas del índice
              help               - Mostrar esta ayuda
              quit/exit          - Salir del programa
            """;

    private static final String PROMPT_MSG = "RAG> ";

    /**
     * Constructor.
     *
     * @param ragEngine Motor CodeRagEngine (puede ser null para usar default indexación)
     */
    public RagRunner(CodeRagEngine ragEngine) {
        this.ragEngine = ragEngine != null ? ragEngine : new CodeRagEngine(new VectorStore(), new Linter(), null);
        this.vectorStore = this.ragEngine.getVectorStore();
    }

    /**
     * Ejecuta el modo interactivo de línea de comandos.
     * <p>
     * Bucle que lee entradas del usuario y procesa comandos hasta que el usuario
     * escriba 'quit' o 'exit'.
 * </p>
     *
     * @throws IOException Si hay error leyendo la entrada del usuario
     */
    public void runInteractiveMode() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Scanner scanner = new Scanner(System.in);

        System.out.println(WELCOME_MSG);

        while (true) {
            System.out.print(PROMPT_MSG);
            String input = reader.readLine();

            if (input == null || input.trim().isEmpty()) {
                continue; // Ignorar entradas vacías
            }

            String command = input.trim();

            // Manejar comandos de salida
            if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit")) {
                System.out.println("👋 Saliendo del motor RAG. ¡Hasta luego!");
                break;
            }

            // Manejar comando de ayuda
            if (command.equalsIgnoreCase("help")) {
                System.out.println(WELCOME_MSG);
                continue;
            }

            // Manejar comando de índice
            if (command.equalsIgnoreCase("index")) {
                reindexProject();
                continue;
            }

            // Manejar comando de estadísticas
            if (command.equalsIgnoreCase("stats")) {
                showIndexStats();
                continue;
            }

            // Manejar búsquedas (deben empezar con "search " o ser una consulta directa)
            if (command.startsWithIgnoreCase("search ")) {
                String query = command.substring("search ".length()).trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                } else {
                    System.out.println("⚠ Error: consulta vacía. Uso: search <consulta>");
                }
                continue;
            }

            // Comando desconocido
            System.out.println("❓ Comando no reconocido: '" + command + "'");
            System.out.println("   Escribe 'help' para ver comandos disponibles.");
        }

        scanner.close();
    }

    /**
     * Ejecuta una búsqueda semántica única y muestra los resultados.
     *
     * @param query Consulta en lenguaje natural
     */
    public void performSearch(String query) {
        System.out.println();
        System.out.println("🔍 Buscando: \"" + query + "\"");
        System.out.println("-".repeat(60));

        try {
            // Ejecutar búsqueda con top-K = 5
            List<VectorStore.SearchResult> results = vectorStore.search(query, 5);

            if (results.isEmpty()) {
                System.out.println("  No se encontraron resultados indexados.");
                System.out.println("   Ejecute 'index' para indexar el proyecto primero.");
                return;
            }

            // Mostrar resultados
            int rank = 1;
            for (VectorStore.SearchResult result : results) {
                System.out.println();
                System.out.println("[" + rank + "] Puntuación: " + String.format("%.4f", result.getSimilarity()));
                System.out.println("    Elemento: " + result.getElementName());
                System.out.println("    Tipo: " + result.getElementType());
                System.out.println("    Fase: " + result.getPhase());
                System.out.println("    Archivo: " + result.getSourceFile());
                System.out.println("    Líneas: " + result.getLineStart() + "-" + result.getLineEnd());
                System.out.println("    Relevancia: " + descriptSimilarity(result.getSimilarity()));
                System.out.println("    Fragmento de código:");
                System.out.println("      " + truncateCode(result.getCodeSnippet()));
                rank++;
            }

            // Mostrar resumen
            double avgSimilarity = results.stream()
                    .mapToDouble(VectorStore.SearchResult::getSimilarity)
                    .average()
                    .orElse(0.0);

            System.out.println();
            System.out.println("--- Resumen ---");
            System.out.println("   Total resultados: " + results.size());
            System.out.println("   Puntuación promedio: " + String.format("%.4f", avgSimilarity));
            System.out.println("   Mejor coincidencia: " + results.get(0).getElementName());

        } catch (Exception e) {
            System.out.println("❌ Error durante la búsqueda: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
    }

    /**
     * Vuelve a indexar todo el proyecto (fases 1-10).
     */
    private void reindexProject() {
        System.out.println("🔄 Volviendo a indexar proyecto (fases 1-10)...");
        try {
            vectorStore.clear();
            ragEngine.indexProjectPhases("1-10", true);
            System.out.println("✅ Re-indexación completada - " + vectorStore.size() + " elementos indexados");
        } catch (Exception e) {
            System.out.println("❌ Error durante re-indexación: " + e.getMessage());
        }
    }

    /**
     * Muestra estadísticas del índice vectorial actual.
     */
    private void showIndexStats() {
        System.out.println("📊 Estadísticas del Índice Vectorial");
        System.out.println("-".repeat(50));

        Map<String, Object> stats = ragEngine.getIndexStats();

        System.out.println("   Elementos indexados: " + stats.get("totalIndexedElements"));
        System.out.println("   Tamaño VectorStore: " + stats.get("vectorStoreSize"));
        System.out.println("   Dimensión de embedding: " + stats.get("embeddingDimension"));
        System.out.println("   Índice tiene contenido: " + stats.get("indexHasContent"));

        // Contar por fase si es posible
        int phase1Count = countByPhase(1);
        int phase10Count = countByPhase(10);
        System.out.println("   Elementos Fase 1: " + phase1Count);
        System.out.println("   Elementos Fase 10: " + phase10Count);

        if (vectorStore.size() > 0) {
            System.out.println();
            System.out.println("   Muestra de búsqueda rápida:");
            System.out.println("   'search System.out.println' → mostraría violaciones de linter");
            System.out.println("   'search Docker sandbox' → mostraría ejemplos de ejecución");
            System.out.println("   'search method naming' → mostraría convenciones camelCase");
        }
    }

    /**
     * Cuenta elementos indexados por fase específica.
     */
    private int countByPhase(int phase) {
        // Contar entradas con la fase especificada
        // Usando reflection para acceder a entries privadas del VectorStore
        try {
            java.lang.reflect.Field field = VectorStore.class.getDeclaredField("entries");
            field.setAccessible(true);
            List<VectorStore.IndexEntry> entries = (List<VectorStore.IndexEntry>) field.get(vectorStore);

            int count = 0;
            for (VectorStore.IndexEntry entry : entries) {
                if (entry.getMetadata().getPhase() == phase) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Convierte una puntuación de similitud a descripción textual.
     */
    private String descriptSimilarity(double similarity) {
        if (similarity >= 0.8) return "Muy alta relevancia";
        if (similarity >= 0.6) return "Alta relevancia";
        if (similarity >= 0.4) return "Relevancia moderada";
        if (similarity >= 0.2) return "Baja relevancia";
        return "Mínima relevancia";
    }

    /**
     * Trunca un fragmento de código para display en consola.
     */
    private String truncateCode(String code) {
        if (code == null || code.length() <= 60) return code;
        return code.substring(0, 57) + "...";
    }

    // Extensión útil de String para comparación case-insensitive de prefijo
    private static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) return false;
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * Punto de entrada principal para el motor RAG interactivo.
     *
     * @args Argumentos de línea de comandos:
     *         - index: Solo indexar y salir
     *         - stats: Solo mostrar estadísticas y salir
     *         - query "<consulta>": Buscar y mostrar resultados, luego salir
     *         (sin argumentos: modo interactivo)
     */
    public static void main(String[] args) {
        CodeRagEngine ragEngine = new CodeRagEngine(new VectorStore(), new Linter(), null);
        RagRunner runner = new RagRunner(ragEngine);

        // Manejar argumentos de línea de comandos
        if (args.length > 0) {
            String firstArg = args[0];

            if (firstArg.equalsIgnoreCase("index")) {
                // Indexar y salir
                try {
                    System.out.println("🔄 Indexando proyecto (fases 1-10)...");
                    ragEngine.indexProjectPhases("1-10", true);
                    System.out.println("✅ Indexación completada - " + ragEngine.getVectorStore().size() + " elementos");
                } catch (Exception e) {
                    System.out.println("❌ Error durante indexación: " + e.getMessage());
                }
                return;
            }

            if (firstArg.equalsIgnoreCase("stats")) {
                // Mostrar estadísticas y salir
                System.out.println("📊 Mostrando estadísticas del índice...");
                ragRunner.showIndexStats();
                return;
            }

            if (firstArg.equalsIgnoreCase("query") && args.length > 1) {
                // Buscar con consulta específica y salir
                String query = String.join(" ", Arrays.asList(args).subList(1, args.length));
                System.out.println("🔍 Ejecutando búsqueda: \"" + query + "\"");
                runner.performSearch(query);
                return;
            }
        }

        // Modo interactivo por defecto
        try {
            runner.runInteractiveMode();
        } catch (IOException e) {
            System.out.println("❌ Error leyendo entrada: " + e.getMessage());
        }
    }
}