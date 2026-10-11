package com.simdev.java.basic.linter;

/**
 * Representa una violación detectada por el linter.
 * Contiene información sobre el código analizado, la regla violada,
 * la severidad y la sugerencia de corrección.
 */
public class AuditViolation {

    private String lineNumber;
    private String columnNumber;
    private String codeSnippet;
    private LinterRule rule;
    private String severity;
    private String message;
    private String suggestion;

    public AuditViolation(String lineNumber, String columnNumber, String codeSnippet,
                          LinterRule rule, String severity, String message, String suggestion) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.codeSnippet = codeSnippet;
        this.rule = rule;
        this.severity = severity;
        this.message = message;
        this.suggestion = suggestion;
    }

    // Getters públicos
    public String getLineNumber() { return lineNumber; }
    public String getColumnNumber() { return columnNumber; }
    public String getCodeSnippet() { return codeSnippet; }
    public LinterRule getRule() { return rule; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public String getSuggestion() { return suggestion; }

    @Override
    public String toString() {
        return "AuditViolation{" +
                "line=" + lineNumber +
                ", column=" + columnNumber +
                ", rule=" + rule.getName() +
                ", severity=" + severity +
                ", message='" + message + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }
}