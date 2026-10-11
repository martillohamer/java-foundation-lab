package com.simdev.java.basic.linter;

/**
 * Representa una regla de linting individual con su descripción y severidad.
 * Cada regla evala un aspecto específico de Clean Code y buenas prácticas.
 */
public class LinterRule {

    private String id;
    private String name;
    private String description;
    private Severity severity;
    private String category;

    // Severidad: LOW, MEDIUM, HIGH, CRITICAL
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public LinterRule(String id, String name, String description, Severity severity, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.severity = severity;
        this.category = category;
    }

    // Getters públicos
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public String getCategory() { return category; }

    // Método para evaluar si un fragmento de código viola esta regla
    public AuditViolation evaluate(String codeSnippet, String context) {
        // Método base - las subclasses sobrescribirán este método
        return null;
    }

    @Override
    public String toString() {
        return "LinterRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", severity=" + severity +
                ", category='" + category + '\'' +
                '}';
    }
}