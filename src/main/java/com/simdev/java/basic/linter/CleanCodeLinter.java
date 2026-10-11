package com.simdev.java.basic.linter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de análisis estático de código Java para detectar violaciones
 * de Clean Code y buenas prácticas.
 * <p>
 * Soporta el análisis de:
 * - Uso de System.out.println (recomendar logging)
 * - Convenciones de nombres (Clases, métodos, variables)
 * - Longitud de métodos
 * - Complejidad ciclomática y anidamiento de bucles/condicionales
 * - Otras métricas de calidad de código
 </p>
 */
public class CleanCodeLinter {

    private List<LinterRule> rules;
    private int totalLinesAnalyzed;
    private int totalMethodsFound;

    public CleanCodeLinter() {
        this.rules = new ArrayList<>();
        // Registrar reglas por defecto
        initializeDefaultRules();
    }

    /**
     * Inicializa las reglas de linting por defecto.
     */
    private void initializeDefaultRules() {
        // Regla 1: Evitar System.out.println
        LinterRule noPrintlnRule = new LinterRule(
                "NO-PRINTLN",
                "Evitar System.out.println",
                "Se detecta uso de System.out.println. Se recomienda usar un framework de logging (SLF4J, Log4j2).",
                LinterRule.Severity.HIGH,
                "logging"
        );
        rules.add(noPrintlnRule);

        // Regla 2: Convención de nombres de clases
        LinterRule classNamingRule = new LinterRule(
                "CLASS-NAMING",
                "Convención de nombres de clases",
                "Las clases deben comenzar con letra mayúscula (UpperCamelCase).",
                LinterRule.Severity.MEDIUM,
                "naming"
        );
        rules.add(classNamingRule);

        // Regla 3: Métodos demasiado largos
        LinterRule longMethodRule = new LinterRule(
                "LONG-METHOD",
                "Métodos excesivamente largos",
                "Métodos con más de 30 líneas podrían beneficiarse de refactoring.",
                LinterRule.Severity.MEDIUM,
                "structure"
        );
        rules.add(longMethodRule);

        // Regla 4: Anidamiento excesivo de bucles
        LinterRule nestedLoopsRule = new LinterRule(
                "NESTED-LOOPS",
                "Anidamiento excesivo de bucles/condicionales",
                "Bucles o condicionales anidados más de 3 niveles profundos afectan la legibilidad.",
                LinterRule.Severity.MEDIUM,
                "complexity"
        );
        rules.add(nestedLoopsRule);

        // Regla 5: Convención de métodos (camelCase)
        LinterRule methodNamingRule = new LinterRule(
                "METHOD-NAMING",
                "Convención de nombres de métodos",
                "Los métodos deben usar camelCase (empiezan con minúscula).",
                LinterRule.Severity.LOW,
                "naming"
        );
        rules.add(methodNamingRule);

        // Regla 6: Variables miembro en camelCase
        LinterRule fieldNamingRule = new LinterRule(
                "FIELD-NAMING",
                "Convención de nombres de campos/atributos",
                "Los campos de clase deben usar camelCase y prefijo opcional 'm' o 'field'.",
                LinterRule.Severity.LOW,
                "naming"
        );
        rules.add(fieldNamingRule);
    }

    /**
     * Analiza el código fuente Java (como String o contenido de archivo) y genera un reporte.
     *
     * @param sourceCode Código Java a analizar
     * @param fileName   Nombre del archivo (para el reporte)
     * @return AuditReport con todas las violaciones y puntuación
     */
    public AuditReport analyzeSource(String sourceCode, String fileName) {
        AuditReport report = new AuditReport(fileName);
        totalLinesAnalyzed = countLines(sourceCode);

        // Dividir código en líneas para análisis línea por línea
        String[] lines = sourceCode.split("\n", -1);
        totalLinesAnalyzed = lines.length;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1; // 1-indexed

            // Aplicar cada regla
            for (LinterRule rule : rules) {
                AuditViolation violation = rule.evaluate(line, "L" + lineNumber);
                if (violation != null) {
                    report.addViolation(violation);
                    // Ajustar score de calidad
                    double penalty = switch (rule.getSeverity()) {
                        case HIGH -> 5.0;
                        case MEDIUM -> 3.0;
                        case LOW -> 1.0;
                        default -> 2.0;
                    };
                    report.setQualityScore(report.getQualityScore() - penalty);
                }
            }
        }

        // Calcular score final (mínimo 0)
        if (report.getQualityScore() < 0) report.setQualityScore(0);
        if (report.getQualityScore() > 100) report.setQualityScore(100);

        // Contar líneas violadas basadas en score
        report.setViolatedLines((int) (totalLinesAnalyzed * (1 - report.getQualityScore() / 100)));

        // Actualizar categorías
        updateCategoryCounts(report);

        return report;
    }

    /**
     * Analiza un archivo Java completo leyendo su contenido.
     *
     * @param filePath Ruta al archivo Java
     * @return AuditReport con resultados
     */
    public AuditReport analyzeFile(String filePath) {
        try {
            String content = java.nio.file.Files.readString(java.nio.file.Paths.get(filePath));
            return analyzeSource(content, java.nio.file.Paths.get(filePath).getFileName().toString());
        } catch (Exception e) {
            // Reporte de error
            AuditReport errorReport = new AuditReport(java.nio.file.Paths.get(filePath).getFileName().toString());
            errorReport.setQualityScore(0);
            errorReport.setViolatedLines(0);
            return errorReport;
        }
    }

    /**
     * Cuenta el número de líneas en el código fuente.
     */
    private int countLines(String sourceCode) {
        if (sourceCode == null || sourceCode.isEmpty()) return 0;
        // Contar saltos de línea, manejando tanto \n como \r\n
        int lines = 1; // Al menos una línea si hay contenido
        for (int i = 0; i < sourceCode.length(); i++) {
            char c = sourceCode.charAt(i);
            if (c == '\n') {
                lines++;
                // Si el siguiente es \r, saltarlo
                if (i + 1 < sourceCode.length() && sourceCode.charAt(i + 1) == '\r') i++;
            }
        }
        return lines;
    }

    /**
     * Actualiza los conteos por categoría en el reporte.
     */
    private void updateCategoryCounts(AuditReport report) {
        // Contar violaciones por categoría
        var counts = new java.util.HashMap<String, Integer>();
        for (AuditViolation v : violations) {
            String category = v.getRule().getCategory();
            counts.merge(category, 1, Integer::sum);
        }
        report.setCategoryCounts(counts);
    }

    // Getters
    public List<LinterRule> getRules() { return rules; }
    public int getTotalLinesAnalyzed() { return totalLinesAnalyzed; }
    public int getTotalMethodsFound() { return totalMethodsFound; }

    // Métodos estáticos de utilidad para análisis rápido

    /**
     * Cuenta ocurrencias de System.out.println en el código.
     *
     * @param sourceCode Código a analizar
     * @return Número de ocurrencias
     */
    public static int countPrintStatements(String sourceCode) {
        if (sourceCode == null) return 0;
        String pattern = "System\\.out\\.println\\(.*?\\)";
        Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(sourceCode);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    /**
     * Verifica si un nombre de clase sigue convención UpperCamelCase.
     *
     * @param className Nombre de la clase a verificar
     * @return true si sigue la convención
     */
    public static boolean isValidClassName(String className) {
        if (className == null || className.isEmpty()) return false;
        return className.matches("^[A-Z][a-zA-Z0-9]*$");
    }

    /**
     * Verifica si un nombre de método sigue convención camelCase.
     *
     * @param methodName Nombre del método a verificar
     * @return true si sigue la convención
     */
    public static boolean isValidMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) return false;
        // Debe empezar con minúscula y pueden tener letras/números después
        return methodName.matches("^[a-z][a-zA-Z0-9]*$");
    }

    /**
     * Verifica si un nombre de variable/campo sigue camelCase.
     *
     * @param fieldName Nombre de la variable a verificar
     * @return true si sigue la convención
     */
    public static boolean isValidFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return false;
        // Variables de instancia suelen empezar minúscula, constantes Mayúsculas
        // Asumimos camelCase simple (empieza minúscula)
        return fieldName.matches("^[a-z][a-zA-Z0-9_]*$");
    }

    /**
     * Detecta nivel de anidamiento máximo de bucles/condicionales en una línea.
     *
     * @param line Línea de código
     * @return Nivel de anidamiento (0 = ninguno)
     */
    public static int detectNestedDepth(String line) {
        if (line == null) return 0;
        int openBraces = 0;
        int maxDepth = 0;
        
        for (char c : line.toCharArray()) {
            if (c == '{') {
                openBraces++;
                maxDepth = Math.max(maxDepth, openBraces);
            } else if (c == '}') {
                openBraces--;
            }
        }
        return maxDepth;
    }

    /**
     * Obtiene la complejidad ciclomérica aproximada basándose en operadores lógicos.
     *
     * @param sourceCode Código a analizar
     * @return Número de puntos de decisión (if, else, for, while, etc.)
     */
    public static int calculateCyclomaticComplexity(String sourceCode) {
        if (sourceCode == null) return 0;
        
        int complexity = 1; // Base complexity
        String[] operators = {"if", "else", "for", "while", "do", "switch", "case", "&&", "||"};
        
        for (String op : operators) {
            // Patrón para buscar el operador como palabra completa
            String pattern = "\\b" + op + "\\b";
            Matcher matcher = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(sourceCode);
            while (matcher.find()) complexity++;
        }
        return complexity;
    }
}