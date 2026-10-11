package com.simdev.java.basic.linter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reporte estructurado de una auditoría de Clean Code.
 * Contiene todas las violaciones encontradas, métricas de calidad
 * y una puntuación general de 0 a 100.
 */
public class AuditReport {

    private String fileName;
    private String analyzedAt;
    private double qualityScore; // 0.0 a 100.0
    private int totalLines;
    private int violatedLines;
    private List<AuditViolation> violations;
    private Map<String, Integer> categoryCounts; // category -> count

    public AuditReport(String fileName) {
        this.fileName = fileName;
        this.analyzedAt = java.time.LocalDateTime.now().toString();
        this.violations = new ArrayList<>();
        this.totalLines = 0;
        this.violatedLines = 0;
        this.qualityScore = 100.0;
    }

    // Getters y Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public double getQualityScore() { return qualityScore; }
    public void setQualityScore(double qualityScore) {
        this.qualityScore = qualityScore;
        // Ajustar violatedLines basado en score
        this.violatedLines = (int) (totalLines * (1 - qualityScore / 100));
    }

    public int getTotalLines() { return totalLines; }
    public void setTotalLines(int totalLines) { this.totalLines = totalLines; }

    public int getViolatedLines() { return violatedLines; }
    public void setViolatedLines(int violatedLines) { this.violatedLines = violatedLines; }

    public List<AuditViolation> getViolations() { return violations; }
    public void addViolation(AuditViolation violation) {
        this.violations.add(violation);
        // Reducir score por cada violación (peso variable por severidad)
        double penalty = switch (ruleSeverityToNumber(violation.getSeverity())) {
            case "HIGH" -> 5.0;
            case "MEDIUM" -> 3.0;
            case "LOW" -> 1.0;
            default -> 2.0;
        };
        this.qualityScore = Math.max(0, this.qualityScore - penalty);
    }

    public Map<String, Integer> getCategoryCounts() { return categoryCounts; }
    public void setCategoryCounts(Map<String, Integer> categoryCounts) { this.categoryCounts = categoryCounts; }

    /**
     * Convierte la severidad de LinterRule.Severity a número para cálculos.
     */
    private double ruleSeverityToNumber(String severity) {
        return switch (severity) {
            case "HIGH" -> 5.0;
            case "MEDIUM" -> 3.0;
            case "LOW" -> 1.0;
            default -> 2.0;
        };
    }

    /**
     * Genera una representación en texto legible del reporte.
     *
     * @return String con el reporte formateado
     */
    public String generateTextReport() {
        StringBuilder sb = new StringBuilder();

        sb.append("========================================\n");
        sb.append("   AUDITORÍO DE CLEAN CODE - REPORTE\n");
        sb.append("========================================\n\n");

        sb.append("Archivo analizado: ").append(fileName).append("\n");
        sb.append("Fecha/hora:      ").append(analyzedAt).append("\n");
        sb.append("Líneas totales:    ").append(totalLines).append("\n");
        sb.append("Líneas violadas:   ").append(violatedLines).append("\n");
        sb.append("Puntuación Q:      ").append(String.format("%.1f", qualityScore)).append("/100\n");
        sb.append("Nivel:            ").append(getQualityLevel()).append("\n\n");

        sb.append("────────────────────────────────────────\n");
        sb.append("VIOLACIONES DETECTADAS: ").append(violations.size()).append("\n");
        sb.append("────────────────────────────────────────\n\n");

        // Agrupar por severidad
        var severityGroups = violations.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        AuditViolation::getSeverity,
                        java.util.stream.Collectors.counting()));

        sb.append("Por severidad:\n");
        sb.append("  HIGH:  ").append(severityGroups.getOrDefault("HIGH", 0)).append("\n");
        sb.append("  MEDIUM:").append(severityGroups.getOrDefault("MEDIUM", 0)).append("\n");
        sb.append("  LOW:   ").append(severityGroups.getOrDefault("LOW", 0)).append("\n\n");

        // Listar cada violación
        sb.append("Detalle de violaciones:\n");
        sb.append("-".repeat(60)).append("\n");
        for (int i = 0; i < violations.size(); i++) {
            AuditViolation v = violations.get(i);
            sb.append(String.format(" [%d] %s (%-6s) [%s]:\n", i + 1, v.getRule().getName(), v.getSeverity(), v.getMessage()));
            if (v.getSuggestion() != null && !v.getSuggestion().isEmpty()) {
                sb.append("         → Sugerencia: ").append(v.getSuggestion()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("========================================\n");
        sb.append("Puntuación final: ").append(String.format("%.1f", qualityScore)).append("/100\n");
        sb.append("========================================\n");

        return sb.toString();
    }

    private String getQualityLevel() {
        if (qualityScore >= 90) return "Excelente (A)";
        if (qualityScore >= 75) return "Bueno (B)";
        if (qualityScore >= 60) return "Aceptable (C)";
        if (qualityScore >= 50) return "Necesita mejora (D)";
        return "Insatisfactorio (F)";
    }

    /**
     * Genera una representación en formato JSON del reporte.
     *
     * @return String JSON con el reporte estructurado
     */
    public String generateJsonReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"fileName\": \"").append(fileName).append("\",\n");
        sb.append("  \"analyzedAt\": \"").append(analyzedAt).append("\",\n");
        sb.append("  \"qualityScore\": ").append(String.format("%.1f", qualityScore)).append(",\n");
        sb.append("  \"totalLines\": ").append(totalLines).append(",\n");
        sb.append("  \"violatedLines\": ").append(violatedLines).append(",\n");
        sb.append("  \"qualityLevel\": \"").append(getQualityLevel()).append("\",\n");
        sb.append("  \"violations\": [\n");

        for (int i = 0; i < violations.size(); i++) {
            AuditViolation v = violations.get(i);
            sb.append("    {\n");
            sb.append("      \"lineNumber\": \"").append(v.getLineNumber()).append("\",\n");
            sb.append("      \"columnNumber\": \"").append(v.getColumnNumber()).append("\",\n");
            sb.append("      \"codeSnippet\": \"").append(escapeJson(v.getCodeSnippet())).append("\",\n");
            sb.append("      \"ruleId\": \"").append(v.getRule().getId()).append("\",\n");
            sb.append("      \"ruleName\": \"").append(v.getRule().getName()).append("\",\n");
            sb.append("      \"severity\": \"").append(v.getSeverity()).append("\",\n");
            sb.append("      \"message\": \"").append(escapeJson(v.getMessage())).append("\",\n");
            sb.append("      \"suggestion\": \"").append(escapeJson(v.getSuggestion())).append("\"\n");
            sb.append("    }");
            if (i < violations.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ],\n");
        sb.append("  \"categoryCounts\": ").append(categoryCountsToJson()).append("\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String categoryCountsToJson() {
        if (categoryCounts == null || categoryCounts.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}