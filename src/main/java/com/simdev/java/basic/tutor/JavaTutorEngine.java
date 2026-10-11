package com.simdev.java.basic.tutor;

import javax.tools.*;
import java.io.*;
import java.util.*;
import java.net.URI;

/**
 * Motor de tutoría Java que compila código en memoria y provee diagnósticos.
 * Utiliza la API estándar javax.tools.JavaCompiler para analizar código fuente
 * y capturar errores de compilación con información estructurada.
 */
public class JavaTutorEngine {

    /**
     * Compila código Java en memoria y retorna los errores estructurados.
     *
     * @param sourceCode Código fuente Java como String
     * @return Lista de errores de compilación, o lista vacía si la compilación exitosa
     */
    public List<CompilationError> compileSourceCode(String sourceCode) {
        List<CompilationError> errors = new ArrayList<>();

        // Configurar el compilador Java standard
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            // Fallback: retornar lista vacía si no hay compilador disponible
            return errors;
        }

        // Crear un file manager en memoria
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // Crear un objeto de diagnóstico para capturar errores
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        // Crear un file object de source code usando nuestra clase interna
        JavaSourceFileObject sourceFile = new JavaSourceFileObject("TestCode.java", sourceCode, fileManager);

        // Intentar compilar
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(sourceFile);
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                Collections.singletonList("-d"),
                Collections.singletonList("."),
                compilationUnits);

        boolean success = task.call();

        // Cerrar recursos
        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Procesar errores si los hay
        if (!success) {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                CompilationError error = new CompilationError();
                error.setLineNumber((int) diagnostic.getLineNumber());
                error.setMessage(diagnostic.getCode() + ": " + diagnostic.getMessage(null));
                error.setType(getErrorType(diagnostic.getCode().toString()));
                error.setDescription(diagnostic.getKind() + ": " + diagnostic.getMessage(null));
                errors.add(error);
            }
        }

        return errors;
    }

    /**
     * Obtiene un mensaje descriptivo en español para el tipo de error dado.
     *
     * @param errorType Código/type of the error
     * @return Mensaje descriptivo en español
     */
    private String getErrorType(String errorType) {
        switch (errorType) {
            case "warning":
            case "note":
                return "Información";
            case "error":
                return "Error";
            case "deprecation":
                return "Deprecation";
            default:
                return "Error";
        }
    }

    /**
     * Proporciona sugerencias en español para errores comunes de Java.
     *
     * @param errorMessages Messages describiendo el error
     * @return Sugerencia descriptiva en español
     */
    public String getSpanishSuggestion(String errorMessage) {
        String lowerMsg = errorMessage.toLowerCase();

        // Falta de punto y coma
        if (lowerMsg.contains("missing") || lowerMsg.contains(";)") ||
                lowerMsg.contains("not found;")) {
            return "Sugerencia: Falta un punto y coma ';' al final de la línea. Verifica la sintaxis de la sentencia.";
        }

        // Llave sin cerrar
        if (lowerMsg.contains("not a statement") || lowerMsg.contains("unexpected type")
                || lowerMsg.contains("reached end of file while parsing")) {
            return "Sugerencia: Llave '{' o '}' sin cerrar o mal colocada. Verifica la estructura de bloques del código.";
        }

        // Tipo incompatible
        if (lowerMsg.contains("incompatible types") || lowerMsg.contains("cannot find symbol")) {
            return "Sugerencia: Tipo de dato incompatibles o variable no declarada. Verifica los tipos de datos y los nombres de las variables.";
        }

        // Variable no declarada
        if (lowerMsg.contains("cannot find symbol") || lowerMsg.contains("symbol: variable")) {
            return "Sugerencia: Variable no declarada o fuera de ámbito. Verifica la declaración y el alcance de la variable.";
        }

        // Error de sintaxis general
        if (lowerMsg.contains("syntax error") || lowerMsg.contains("illegal start of type")) {
            return "Sugerencia: Error de sintaxis general. Revisa la estructura general del código, incluyendo importaciones y clases.";
        }

        // Bucle infinito o estructura de control
        if (lowerMsg.contains("not a valid loop") || lowerMsg.contains("incompatible with")) {
            return "Sugerencia: Problema con la estructura de control (bucles o condiciones). Verifica los tipos de las variables en los bucles.";
        }

        // Default - sugerencia genérica
        return "Sugerencia: Revisa el error mostrado arriba. Para mejores resultados, analiza el código línea por línea.";
    }

    /**
     * Diagnostica código fuente y retorna un reporte completo con errores y sugerencias.
     *
     * @param sourceCode Código fuente Java a analizar
     * @return Reporte de diagnóstico con errores y sugerencias
     */
    public DiagnosticReport diagnose(String sourceCode) {
        List<CompilationError> errors = compileSourceCode(sourceCode);
        DiagnosticReport report = new DiagnosticReport();

        report.setSourceCode(sourceCode);
        report.setCompilationSuccessful(errors.isEmpty());

        if (!errors.isEmpty()) {
            report.setErrors(errors);

            // Agregar sugerencias para cada error
            for (CompilationError error : errors) {
                String suggestion = getSpanishSuggestion(error.getMessage());
                error.setSuggestion(suggestion);
            }
        }

        return report;
    }

    /**
     * Clase interna que representa el source code como objeto JavaFileObject.
     * Permite compilar código Java desde un String en lugar de un archivo físico.
     */
public static class JavaSourceFileObject extends SimpleJavaFileObject {
        private final String sourceCode;
        private final JavaFileManager fileManager;

        /**
         * Crea un nuevo JavaSourceFileObject para el código fuente especificado.
         *
         * @param className nombre de la clase
         * @param sourceCode  código fuente Java
         * @param fileManager file manager asociado
         */
        public JavaSourceFileObject(String className, String sourceCode, JavaFileManager fileManager) {
            super(URI.create("string:///" + className.replace('.', '/') + "/code/java"), JavaFileObject.Kind.SOURCE);
            this.sourceCode = sourceCode;
            this.fileManager = fileManager;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return sourceCode;
        }
    }

    /**
     * Clase interna que representa un error de compilación estructurado.
     */
    public static class CompilationError {
        private int lineNumber;
        private String message;
        private String type;
        private String description;
        private String suggestion;

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }
    }

    /**
     * Clase que representa un reporte de diagnóstico completo.
     */
    public static class DiagnosticReport {
        private String sourceCode;
        private boolean compilationSuccessful;
        private List<CompilationError> errors;
        private String summary;

        public String getSourceCode() {
            return sourceCode;
        }

        public void setSourceCode(String sourceCode) {
            this.sourceCode = sourceCode;
        }

        public boolean isCompilationSuccessful() {
            return compilationSuccessful;
        }

        public void setCompilationSuccessful(boolean compilationSuccessful) {
            this.compilationSuccessful = compilationSuccessful;
        }

        public List<CompilationError> getErrors() {
            return errors;
        }

        public void setErrors(List<CompilationError> errors) {
            this.errors = errors;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}