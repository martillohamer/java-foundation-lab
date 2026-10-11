package com.simdev.java.basic.tutor;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para JavaTutorEngine.
 * Verifica que el motor de tutoría detecte y proporcione sugerencias
 * para errores de sintaxis Java comunes.
 */
class JavaTutorEngineTest {

    private JavaTutorEngine tutorEngine;

    @BeforeEach
    void setUp() {
        tutorEngine = new JavaTutorEngine();
    }

    @Test
    @DisplayName("Test: Detectar error de punto y coma faltante")
    void testDetectMissingSemicolon() {
        // Código con error intencional: falta de punto y coma
        String sourceCode = "public class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "        int x = 10\n" +  // ERROR: falta ;
                "        System.out.println(x);\n" +
                "    }\n" +
                "}";

        DiagnosticReport report = tutorEngine.diagnose(sourceCode);

        assertFalse(report.isCompilationSuccessful(), "La compilación debe fallar con error de sintaxis");
        assertTrue(report.getErrors().size() > 0, "Debe haber al menos un error de compilación");

        // Verificar que el error contiene información sobre punto y coma
        boolean hasSemicolonError = report.getErrors().stream()
                .anyMatch(error -> error.getMessage().toLowerCase().contains("missing") ||
                        error.getMessage().toLowerCase().contains(";"));

        assertTrue(hasSemicolonError, "El error debe mencionar la falta de punto y coma");

        // Verificar que haya sugerencia en español
        boolean hasSuggestion = report.getErrors().stream()
                .anyMatch(error -> error.getSuggestion() != null && !error.getSuggestion().isEmpty());

        assertTrue(hasSuggestion, "Debe haber una sugerencia en español para el error");
    }

    @Test
    @DisplayName("Test: Detectar variable no declarada")
    void testDetectUndefinedVariable() {
        // Código con error intencional: variable no declarada
        String sourceCode = "public class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(undefinedVariable);\n" +  // ERROR: variable no declarada
                "    }\n" +
                "}";

        DiagnosticReport report = tutorEngine.diagnose(sourceCode);

        assertFalse(report.isCompilationSuccessful(), "La compilación debe fallar");
        assertTrue(report.getErrors().size() > 0, "Debe haber un error de compilación");

        // Verificar mensaje sobre variable no encontrada
        boolean hasVariableError = report.getErrors().stream()
                .anyMatch(error -> error.getMessage().toLowerCase().contains("cannot find symbol") ||
                        error.getMessage().toLowerCase().contains("variable"));

        assertTrue(hasVariableError, "El error debe mencionar variable no declarada");

        // Verificar sugerencia
        boolean hasSuggestion = report.getErrors().stream()
                .anyMatch(error -> error.getSuggestion() != null);

        assertTrue(hasSuggestion, "Debe haber sugerencia en español");
    }

    @Test
    @DisplayName("Test: Código válido compila correctamente")
    void testValidCodeCompiles() {
        // Código válido sin errores
        String sourceCode = "public class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "        int x = 10;\n" +
                "        System.out.println(x);\n" +
                "    }\n" +
                "}";

        DiagnosticReport report = tutorEngine.diagnose(sourceCode);

        assertTrue(report.isCompilationSuccessful(), "El código válido debe compilar exitosamente");
        assertTrue(report.getErrors().isEmpty(), "No debe haber errores de compilación");
    }

    @Test
    @DisplayName("Test: Detectar llave sin cerrar")
    void testDetectMissingBrace() {
        // Código con error intencional: llave sin cerrar
        String sourceCode = "public class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "        if (true) {\n" +
                "            System.out.println(\"Hello\");\n" +
                "    } // ERROR: llave cerradora falta para el main\n" +
                "    }\n" +
                "}";

        DiagnosticReport report = tutorEngine.diagnose(sourceCode);

        assertFalse(report.isCompilationSuccessful(), "La compilación debe fallar con error de sintaxis");
        assertTrue(report.getErrors().size() > 0, "Debe haber un error de compilación");

        // Verificar que haya sugerencia
        boolean hasSuggestion = report.getErrors().stream()
                .anyMatch(error -> error.getSuggestion() != null && !error.getSuggestion().isEmpty());

        assertTrue(hasSuggestion, "Debe haber sugerencia en español para el error de llave");
    }

    @Test
    @DisplayName("Test: Reporte tiene estructura completa")
    void testReportStructure() {
        String sourceCode = "public class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "        int x = 10;\n" +
                "    }\n" +
                "}";

        DiagnosticReport report = tutorEngine.diagnose(sourceCode);

        // Verificar que el reporte tenga los campos esperados
        assertNotNull(report.getSourceCode(), "El reporte debe tener el código fuente");
        assertTrue(report.isCompilationSuccessful() || report.getErrors() != null, "O bien compila o hay errores");
        assertNotNull(report.getSummary(), "El reporte debe tener un resumen");

        // Si hay errores, verificar que tengan estructura completa
        if (!report.getErrors().isEmpty()) {
            for (JavaTutorEngine.CompilationError error : report.getErrors()) {
                assertNotNull(error.getLineNumber(), "El error debe tener número de línea");
                assertNotNull(error.getMessage(), "El error debe tener mensaje");
                assertNotNull(error.getType(), "El error debe tener tipo");
                assertNotNull(error.getSuggestion(), "El error debe tener sugerencia en español");
            }
        }
    }
}