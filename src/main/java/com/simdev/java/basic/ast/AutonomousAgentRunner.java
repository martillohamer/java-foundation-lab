package com.simdev.java.basic.ast;

import com.github.javaparser.Parser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ParseException;
import com.simdev.java.basic.linter.*;
import java.io.*;
import java.nio.file.*;

/**
 * Ejecutor autónomo de agente IA para refactorización basada en AST.
 * <p>
 * Este agente ejecuta un flujo autónomo completo:
 * 1. Analiza el archivo fuente utilizando el Linter (Fase 6)
 * 2. Refactoriza el AST automáticamente usando el AstRefactoringEngine
 * 3. Guarda los cambios en una clase temporal
 * 4. Valida la sintaxis mediante el compilador de la plataforma (javac)
 * 5. Genera un informe estructurado con antes/después de la optimización
 * </p>
 * <p>
 * El agente está diseñado para operar de forma independiente y puede ser
 * invocado desde la línea de comandos o a través de la API REST como parte
 * del enjambre de agentes IA (Fase 10).
 * </p>
 */
public class AutonomousAgentRunner {

    private final Path sourceFile;
    private final Linter linter;
    private final Path tempOutputDir;

    /**
     * Construye un nuevo ejecutor autónomo.
     *
     * @param sourceFile Ruta al archivo Java source a procesar
     * @param linter     Instancia del Linter para análisis estático (puede ser null para usar defaults)
     * @param tempDir    Directorio temporal para guardar resultados intermedios (opcional)
     */
    public AutonomousAgentRunner(Path sourceFile, Linter linter, Path tempDir) {
        this.sourceFile = sourceFile;
        this.linter = linter != null ? linter : new Linter();
        this.tempOutputDir = tempDir != null ? tempDir : Paths.get(System.getProperty("java.io.tmpdir", "tmp"));
    }

    /**
     * Ejecuta el flujo autónomo completo de refactorización.
     * <p>
     * El flujo paso a paso es:
     * <ol>
     *   <li>Analyze: Leer y analizar el archivo fuente con el Linter</li>
     *   <li>Refactor: Aplicar transformaciones automáticas basadas en el análisis AST</li>
     *   <li>Save: Guardar el código refactorizado en un archivo temporal</li>
     *   <li>Validate: Verificar la sintaxis compilando con javac de la plataforma</li>
     *   <li>Report: Generar informe estructurado con before/after optimization</li>
     * </ol>
     * </p>
     *
     * @return Resultado estructurado con todos los pasos y hallazgos
     * @throws IOException Si hay error en operaciones de archivo
     * @throws InterruptedException Si el proceso de compilación es interrumpido
     */
    public AgentResult runAutonomousFlow() throws IOException, InterruptedException {
        System.out.println("=" .repeat(60));
        System.out.println("  EJECUTOR AUTÓNOMO DE REFACTORIZACIÓN BASED ON AST (Fase 10)");
        System.out.println("=" .repeat(60));
        System.out.println();

        // Paso 1: Analizar el archivo fuente
        System.out.println("[PASO 1] Analizando archivo fuente: " + sourceFile.getFileName());
        AuditReport originalReport;
        try {
            originalReport = linter.analyzeSource(Files.readString(sourceFile), sourceFile.getFileName().toString());
            System.out.println("    ✅ Análisis completado - " + originalReport.getViolations().size() + " violaciones detectadas");
            System.out.println("       Puntuación de calidad: " + String.format("%.1f", originalReport.getQualityScore()) + "/100");
        } catch (Exception e) {
            System.out.println("    ❌ Error al analizar archivo: " + e.getMessage());
            return createErrorResult("Error analyzing source file: " + e.getMessage());
        }

        // Paso 2: Refactorizar el AST automáticamente
        System.out.println();
        System.out.println("[PASO 2] Ejecutando refactorización automática AST...");
        AstRefactoringEngine engine;
        try {
            engine = new AstRefactoringEngine(sourceFile, linter);
            RefactoringResult refactoringResult = engine.refactor();

            System.out.println("    ✅ Refactorización completada - " +
                    refactoringResult.getTransformationCount() + " transformaciones planeadas");
            System.out.println("       " + refactoringResult.getAuditReport().getViolations().size() + " violaciones abordadas");
        } catch (Exception e) {
            System.out.println("    ❌ Error durante la refactorización: " + e.getMessage());
            return createErrorResult("Error during AST refactoring: " + e.getMessage());
        }

        // Paso 3: Guardar cambios en clase temporal
        System.out.println();
        System.out.println("[PASO 3] Guardando código refactorizado en archivo temporal...");
        Path tempRefactoredFile;
        try {
            // Crear nombre de archivo temporal basado en el original
            String tempFileName = "refactored_" + sourceFile.getFileName().toString().replace(".java", "") + "_"
                    + System.currentTimeMillis() + ".java";
            tempRefactoredFile = this.tempOutputDir.resolve(tempFileName);

            // Guardar código refactorizado
            String refactoredCode = refactoringResult.getRefactoredCode();
            Files.writeString(tempRefactoredFile, refactoredCode);

            System.out.println("    ✅ Código guardado en: " + tempRefactoredFile.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("    ❌ Error al guardar archivo temporal: " + e.getMessage());
            return createErrorResult("Error saving refactored file: " + e.getMessage());
        }

        // Paso 4: Validar sintaxis mediante compilador de la plataforma
        System.out.println();
        System.out.println("[PASO 4] Validando sintaxis con compilador de la plataforma (javac)...");
        boolean compilationValid = false;
        String compilationOutput = "";
        try {
            // Usar javac del sistema para validar
            ProcessBuilder pb = new ProcessBuilder(
                    "javac",
                    "-release", "17",
                    tempRefactoredFile.toAbsolutePath().toString()
            );
            pb.directory(sourceFile.getParent().toFile()); // Compilar en directorio del proyecto
            Process process = pb.start();

            // Leer salida y error streams
            compilationOutput = readProcessOutput(process.getInputStream());
            String compilationErrors = readProcessOutput(process.getErrorStream());

            int exitCode = process.waitFor();
            compilationValid = (exitCode == 0);

            if (compilationValid) {
                System.out.println("    ✅ Validación exitosa - Código Java sintácticamente correcto");
            } else {
                System.out.println("    ⚠️ Advertencias/Errores de compilación:");
                System.out.println("       " + compilationOutput);
                if (!compilationErrors.isEmpty()) {
                    System.out.println("       Detalle: " + compilationErrors);
                }
            }
        } catch (Exception e) {
            System.out.println("    ⚠️ No se pudo validar con javac: " + e.getMessage());
            compilationOutput = "Error invoking javac: " + e.getMessage();
        }

        // Paso 5: Generar informe estructurado antes/después
        System.out.println();
        System.out.println("[PASO 5] Generando informe estructurado antes/después de optimización...");
        AgentResult result = compileFinalResult(originalReport, refactoringResult, tempRefactoredFile, compilationValid, compilationOutput);

        System.out.println();
        System.out.println("=" .repeat(60));
        System.out.println("  FLOW COMPLETADO EXITOSAMENTE");
        System.out.println("=" .repeat(60));
        System.out.println();

        return result;
    }

    /**
     * Compila el resultado final agente con toda la información recopilada.
     */
    private AgentResult compileFinalResult(AuditReport originalReport,
                                           RefactoringResult refactoringResult,
                                           Path tempRefactoredFile,
                                           boolean compilationValid,
                                           String compilationOutput) {
        // Calcular métricas de mejora
        double originalScore = originalReport.getQualityScore();
        double newScore = refactoringResult.getAuditReport().getQualityScore();
        double improvement = originalScore - newScore;

        // Contar mejoras vs regresiones
        int originalViolations = originalReport.getViolations().size();
        int newViolations = refactoringResult.getAuditReport().getViolations().size();
        int violationsReduced = originalViolations - newViolations;

        // Determinar estado general
        String overallStatus;
        if (compilationValid && violationsReduced > 0) {
            overallStatus = "SUCCESS - Mejora detectada y código válido";
        } else if (compilationValid) {
            overallStatus = "COMPLETED - Código válido pero sin reducciones significativas";
        } else if (!compilationValid && violationsReduced > 0) {
            overallStatus = "PARTIAL - Mejoras aplicadas pero hay advertencias de compilación";
        } else {
            overallStatus = "REQUIRES REVIEW - Problemas de validación y sin mejoras claras";
        }

        // Crear resultado agente
        AgentResult result = new AgentResult(
                sourceFile,
                originalReport,
                refactoringResult.getRefactoredCode(),
                refactoringResult.getAuditReport(),
                refactoringResult.getTransformations(),
                tempRefactoredFile,
                compilationValid,
                improvement,
                violationsReduced,
                overallStatus,
                compilationOutput
        );

        // Mostrar resumen
        System.out.println("--- RESUMEN DE REFACTORIZACIÓN ---");
        System.out.println("Archivo: " + result.getSourceFile().getFileName());
        System.out.println("Puntuación original: " + String.format("%.1f", originalScore) + "/100");
        System.out.println("Puntuación nueva:    " + String.format("%.1f", newScore) + "/100");
        System.out.println("Mejora:              " + String.format("%.2f", improvement) + " puntos");
        System.out.println("Violaciones originales: " + originalViolations);
        System.out.println("Violaciones nuevas:    " + newViolations);
        System.out.println("Violaciones reducidas: " + violationsReduced);
        System.out.println("Compilación exitosa:   " + compilationValid);
        System.out.println("Estado general:        " + overallStatus);
        System.out.println("-------------------------");

        return result;
    }

    /**
     * Lee la salida de un proceso.
     */
    private String readProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Crea un resultado de error.
     */
    private AgentResult createErrorResult(String errorMessage) {
        try {
            // Aún intentamos crear un resultado básico
            AuditReport errorReport = new AuditReport("error.java");
            errorReport.setQualityScore(0);
            errorReport.addViolation(new AuditViolation("N/A", "N/A", "N/A",
                    new LinterRule("ERROR", "Error del Agente", "Error during autonomous execution",
                            LinterRule.Severity.CRITICAL, "agent"), errorMessage, "Review manually"));

            return new AgentResult(
                    sourceFile,
                    errorReport,
                    Files.readString(sourceFile), // Código original en caso de error
                    errorReport,
                    new java.util.ArrayList<>(),
                    null,
                    false,
                    0.0,
                    0,
                    "ERROR - " + errorMessage,
                    errorMessage
            );
        } catch (IOException e) {
            // Fallback último recurso
            return new AgentResult(
                    sourceFile,
                    new AuditReport("error.java"),
                    "/* Error: Could not read source file */",
                    new AuditReport("error.java"),
                    new java.util.ArrayList<>(),
                    null,
                    false,
                    0.0,
                    0,
                    "CRITICAL ERROR",
                    errorMessage
            );
        }
    }

    // Getters
    public Path getSourceFile() { return sourceFile; }
    public Linter getLinter() { return linter; }
    public Path getTempOutputDir() { return tempOutputDir; }

    /**
     * Clase de resultado agente que encapsula todo el flujo.
     */
    public static class AgentResult {
        private final Path sourceFile; // Archivo fuente original
        private final AuditReport originalReport; // Reporte original antes de refactorizar
        private final String refactoredCode; // Código refactorizado (después)
        private final AuditReport newReport; // Reporte después de refactorizar
        private final List<AstRefactoringEngine.AstTransformation> transformations; // Transformaciones aplicadas
        private final Path tempRefactoredFile; // Archivo temporal con código refactorizado
        private final boolean compilationValid; // ¿La compilación fue exitosa?
        private final double qualityImprovement; // Mejora en puntuación (puntos)
        private final int violationsReduced; // Reducción de violaciones
        private final String overallStatus; // Estado general del flujo
        private final String compilationDetails; // Detalles devalidación de compilación

        // Constructor
        public AgentResult(Path sourceFile, AuditReport originalReport, String refactoredCode,
                           AuditReport newReport,
                           List<AstRefactoringEngine.AstTransformation> transformations,
                           Path tempRefactoredFile, boolean compilationValid,
                           double qualityImprovement, int violationsReduced,
                           String overallStatus, String compilationDetails) {
            this.sourceFile = sourceFile;
            this.originalReport = originalReport;
            this.refactoredCode = refactoredCode;
            this.newReport = newReport;
            this.transformations = transformations;
            this.tempRefactoredFile = tempRefactoredFile;
            this.compilationValid = compilationValid;
            this.qualityImprovement = qualityImprovement;
            this.violationsReduced = violationsReduced;
            this.overallStatus = overallStatus;
            this.compilationDetails = compilationDetails;
        }

        // Getters completos
        public Path getSourceFile() { return sourceFile; }
        public AuditReport getOriginalReport() { return originalReport; }
        public String getRefactoredCode() { return refactoredCode; }
        public AuditReport getNewReport() { return newReport; }
        public List<AstRefactoringEngine.AstTransformation> getTransformations() { return transformations; }
        public Path getTempRefactoredFile() { return tempRefactoredFile; }
        public boolean isCompilationValid() { return compilationValid; }
        public double getQualityImprovement() { return qualityImprovement; }
        public int getViolationsReduced() { return violationsReduced; }
        public String getOverallStatus() { return overallStatus; }
        public String getCompilationDetails() { return compilationDetails; }

        /**
         * Obtiene el porcentaje de mejora en calidad.
         *
         * @return Mejora porcentual (puede ser negativa si empeoró)
         */
        public double getQualityImprovementPercentage() {
            if (originalReport.getQualityScore() == 0) return 0;
            return ((originalReport.getQualityScore() - newReport.getQualityScore())
                    / originalReport.getQualityScore()) * 100;
        }

        /**
         * Genera un informe JSON estructurado para el agente.
         *
         * @return String JSON con el informe completo
         */
        public String generateJsonReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"sourceFile\": \"").append(sourceFile.getFileName().toString()).append("\",\n");
            sb.append("  \"originalQualityScore\": ").append(String.format("%.1f", originalReport.getQualityScore())).append(",\n");
            sb.append("  \"newQualityScore\": ").append(String.format("%.1f", newReport.getQualityScore())).append(",\n");
            sb.append("  \"qualityImprovement\": ").append(String.format("%.2f", qualityImprovement)).append(",\n");
            sb.append("  \"qualityImprovementPercentage\": ").append(String.format("%.1f", getQualityImprovementPercentage())).append(",\n");
            sb.append("  \"violationsReduced\": ").append(violationsReduced).append(",\n");
            sb.append("  " + "\"compilationValid\": " + compilationValid + ",\n");
            sb.append("  \"overallStatus\": \"").append(overallStatus).append("\",\n");
            sb.append("  \"compilationDetails\": \"").append(escapeJson(compilationDetails)).append("\",\n");

            // Transformaciones
            sb.append("  \"transformations\": [\n");
            for (int i = 0; i < transformations.size(); i++) {
                AstRefactoringEngine.AstTransformation t = transformations.get(i);
                sb.append("    {\n");
                sb.append("      \"id\": \"").append(t.getTransformationId()).append("\",\n");
                sb.append("      \"description\": \"").append(escapeJson(t.getDescription())).append("\",\n");
                sb.append("      \"lineNumber\": ").append(t.getLineNumber()).append(",\n");
                sb.append("      \"endLine\": ").append(t.getEndLine()).append(",\n");
                sb.append("      \"type\": \"").append(t.getTransformationType()).append("\"\n");
                sb.append("    }");
                if (i < transformations.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");

            // Violinaciones originales
            sb.append("  \"originalViolations\": [\n");
            for (int i = 0; i < originalReport.getViolations().size(); i++) {
                AuditViolation v = originalReport.getViolations().get(i);
                sb.append("    {\n");
                sb.append("      \"ruleId\": \"").append(v.getRule().getId()).append("\",\n");
                sb.append("      \"ruleName\": \"").append(v.getRule().getName()).append("\",\n");
                sb.append("      \"severity\": \"").append(v.getSeverity()).append("\",\n");
                sb.append("      \"message\": \"").append(escapeJson(v.getMessage())).append("\",\n");
                sb.append("      \"suggestion\": \"").append(escapeJson(v.getSuggestion())).append("\"\n");
                sb.append("    }");
                if (i < originalReport.getViolations().size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");

            // Violinaciones nuevas
            sb.append("  \"newViolations\": [\n");
            for (int i = 0; i < newReport.getViolations().size(); i++) {
                AuditViolation v = newReport.getViolations().get(i);
                sb.append("    {\n");
                sb.append("      \"ruleId\": \"").append(v.getRule().getId()).append("\",\n");
                sb.append("      \"ruleName\": \"").append(v.getRule().getName()).append("\",\n");
                sb.append("      \"severity\": \"").append(v.getSeverity()).append("\",\n");
                sb.append("      \"message\": \"").append(escapeJson(v.getMessage())).append("\",\n");
                sb.append("      \"suggestion\": \"").append(escapeJson(v.getSuggestion())).append("\"\n");
                sb.append("    }");
                if (i < newReport.getViolations().size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]\n");

            sb.append("}\n");
            return sb.toString();
        }

        private String escapeJson(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        }
    }
}