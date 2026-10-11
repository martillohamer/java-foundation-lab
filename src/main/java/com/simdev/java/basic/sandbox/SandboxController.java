package com.simdev.java.basic.sandbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para la ejecución segura de código Java en sandbox.
 * Expone endpoints para compilar y ejecutar código fuente Java.
 * <p>
 * Endpoints disponibles:
 * - POST /api/sandbox/execute: Ejecutar código Java completo
 * - POST /api/sandbox/compile: Solo compilar código Java
 * - GET /api/sandbox/health: Verificar estado del sandbox
 * </p>
 */
@RestController
@RequestMapping("/api/sandbox")
public class SandboxController {

    @Autowired
    private SandboxService sandboxService;

    /**
     * Ejecutar código fuente Java completo.
     * Recibe un JSON con el campo "source" que contiene el código Java.
     * <p>
     * Ejemplo de request:
     * {
     *   "source": "public class Main { public static void main(String[] args) { System.out.println(\"Hello\"); } }"
     * }
     * </p>
     *
     * @param request Body JSON con el campo "source" (código Java)
     * @return ResponseEntity con el resultado de ejecución en formato JSON
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCode(@RequestBody ExecutionRequest request) {
        if (request == null || request.getSource() == null || request.getSource().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ExecutionResponse("Error: No se proporcionó código fuente", false, "Main", new ArrayList<>()));
        }

        // Ejecutar el código en sandbox
        SandboxService.ExecutionResult result = sandboxService.executeJavaCode(request.getSource());

        // Convertir a formato de respuesta API
        ExecutionResponse apiResponse = new ExecutionResponse(
                result.getOutput(),
                result.isCompilationSuccessful(),
                result.getCompiledClassName(),
                result.getCompilationErrors()
        );

        if (result.isSuccess()) {
            return ResponseEntity.ok(apiResponse);
        } else if (!result.isCompilationSuccessful()) {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(apiResponse);
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(apiResponse);
        }
    }

    /**
     * Endpoint para compilar código Java sin ejecutarlo.
     *
     * @param request Body JSON con el código fuente
     * @return Resultado de compilación
     */
    @PostMapping("/compile")
    public ResponseEntity<CompilationResponse> compileCode(@RequestBody ExecutionRequest request) {
        if (request == null || request.getSource() == null || request.getSource().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new CompilationResponse(false, "Error: No se proporcionó código fuente", new ArrayList<>()));
        }

        SandboxService.CompilationResult compilationResult = sandboxService.compileJavaFile(request.getSource().replace("class Main", "class CompiledMain"));

        CompilationResponse response = new CompilationResponse(
                compilationResult.isSuccess(),
                compilationResult.isSuccess() ? "Compilación exitosa" : "Errores de compilación",
                compilationResult.getErrors()
        );

        if (compilationResult.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(response);
        }
    }

    /**
     * Endpoint de verificación de salud.
     *
     * @return Estado del sandbox
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "Java Sandbox Micro-Engine",
                "version", "1.0.0",
                "compiler", "JavaTutorEngine + javac"
        ));
    }
}

/**
 * Request DTO para entrada de ejecución.
 */
class ExecutionRequest {
    private String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}

/**
 * Response DTO para ejecución de código.
 */
class ExecutionResponse {
    private String output;
    private boolean compilationSuccessful;
    private String compiledClassName;
    private List<String> compilationErrors;

    public ExecutionResponse() {
        this.compilationErrors = new ArrayList<>();
    }

    public ExecutionResponse(String output, boolean compilationSuccessful, String compiledClassName, List<String> compilationErrors) {
        this.output = output;
        this.compilationSuccessful = compilationSuccessful;
        this.compiledClassName = compiledClassName;
        this.compilationErrors = compilationErrors != null ? compilationErrors : new ArrayList<>();
    }

    // Getters y Setters
    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isCompilationSuccessful() {
        return compilationSuccessful;
    }

    public void setCompilationSuccessful(boolean compilationSuccessful) {
        this.compilationSuccessful = compilationSuccessful;
    }

    public String getCompiledClassName() {
        return compiledClassName;
    }

    public void setCompiledClassName(String compiledClassName) {
        this.compiledClassName = compiledClassName;
    }

    public List<String> getCompilationErrors() {
        return compilationErrors;
    }

    public void setCompilationErrors(List<String> compilationErrors) {
        this.compilationErrors = compilationErrors;
    }
}

/**
 * Response DTO para compilación.
 */
class CompilationResponse {
    private boolean success;
    private String message;
    private List<String> errors;

    public CompilationResponse() {
        this.errors = new ArrayList<>();
    }

    public CompilationResponse(boolean success, String message, List<String> errors) {
        this.success = success;
        this.message = message;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}