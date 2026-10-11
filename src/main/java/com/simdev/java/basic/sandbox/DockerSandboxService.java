package com.simdev.java.basic.sandbox;

import com.simdev.java.basic.linter.CleanCodeLinter;
import com.simdev.java.basic.linter.AuditReport;
import com.simdev.java.basic.linter.AuditViolation;
import com.simdev.java.basic.linter.LinterRule;
import com.simdev.java.basic.linter.LinterRule.Severity;
import com.github.ddockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecCommandExec;
import com.github.dockerjava.core.command.ExecCmdExec;
import com.github.docker.java.client.DockerClient;
import com.github.docker.java.client.model.Frame;
import com.github.docker.java.client.model.Frame.MessageType;
import com.github.docker.java.client.model.CreateContainerResponse;
import com.github.docker.java.client.model.HostConfig;
import com.github.docker.java.client.model.Ports;
import com.github.docker.java.client.model.WaitResponse;

import java.io.*;
import java.time.Duration;
import java.util.*;

/**
 * Servicio de sandbox contenerizado usando Docker para ejecución aislada de código Java.
 * <p>
 * En lugar de compilar y ejecutar código localmente, este servicio:
 * 1. Empaqueta el código fuente Java en un contenedor Docker efímero
 * 2. Ejecuta el código dentro del contenedor con recursos limitados
 * 3. Captura stdout, stderr y el código de salida
 * 4. Elimina el contenedor automáticamente después de la ejecución
 * </p>
 * <p>
 * Esta arquitectura provee aislamiento de proceso y seguridad de sistema,
 * ya que el código se ejecuta en un entorno completamente aislado con
 * límites de memoria y tiempo configurables.
 * </p>
 */
public class DockerSandboxService {

    // Configuración por defecto del sandbox
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_MEMORY_MB = 256;
    private static final String DEFAULT_IMAGE = "openjdk:21-jdk-slim";

    private final DockerClient dockerClient;
    private final CleanCodeLinter linter;

    public DockerSandboxService() {
        // Inicializar cliente Docker
        DockerClientConfig config = DockerClientBuilder.getDefaultConfig().build();
        this.dockerClient = DockerClientBuilder.getInstance(config).build();

        // Inicializar linter para validaciones previas
        this.linter = new CleanCodeLinter();
    }

    /**
     * Ejecuta código Java dentro de un contenedor Docker aislado.
     *
     * @param javaSource Código fuente Java completo a ejecutar
     * @param timeoutSegundos Tiempo máximo de ejecución en segundos
     * @param memoryLimiteMB Límite de memoria en MB
     * @return Resultado de ejecución con salida, errores y código de salida
     */
    public ExecutionResult executeJavaCode(String javaSource, int timeoutSegundos, int memoryLimiteMB) {
        if (javaSource == null || javaSource.trim().isEmpty()) {
            return ExecutionResult.error("No se proporcionó código fuente", 1);
        }

        // Validación inicial de Clean Code
        AuditReport reporte = linter.analyzeSource(javaSource, "inline-code");
        if (reporte.getQualityScore() < 50) {
            return ExecutionResult.warning(
                    "Código con baja calidad de Clean Code (score: " + reporte.getQualityScore() + ")",
                    2
            );
        }

        try {
            // Crear y ejecutar contenedor Docker
            String containerId = crearYEjecutarContenedor(javaSource, timeoutSegundos, memoryLimiteMB);

            // Obtener resultados del contenedor
            return obtenerResultadoContenedor(containerId, timeoutSegundos);

        } catch (IOException | InterruptedException e) {
            return ExecutionError.runtimeError("Error en el sandbox Docker: " + e.getMessage());
        } finally {
            // Limpieza garantizada del contenedor
            limpiarContenedor();
        }
    }

    /**
     * Crea y ejecuta un contenedor Docker con el código Java.
     */
    private String crearYEjecutarContenedor(String javaSource, int timeoutSegundos, int memoryLimiteMB)
            throws IOException, InterruptedException {

        // Escribir el código fuente en un archivo temporal dentro del contenedor
        String script = crearScriptEjecucion(javaSource);

        // Configuración del contenedor
        HostConfig hostConfig = HostConfig.newBuilder()
                .withMemory(memoryLimiteMB * 1024 * 1024) // Memory limit in bytes
                .withCpuCount(2L) // CPU limit
                .withBinds(new com.github.docker.java.client.bind.Mount("/tmp/sandbox", com.github.docker.java.client.bind.Mount.Type.BIND))
                .withPublishAllPorts(true)
                .withAutoRemove(true) // Auto-remove container when it exits
                .withSecurityOpt("no-new-privileges:true")
                .withReadonlyRootfs(true);

        // Crear el contenedor
        CreateContainerResponse container = dockerClient.createContainerCmd(DEFAULT_IMAGE)
                .withHostConfig(hostConfig)
                .withCmd("bash", "-c", script)
                .exec();

        String containerId = container.getId();

        // Iniciar el contenedor
        dockerClient.startContainerCmd(containerId).exec();

        return containerId;
    }

    /**
     * Crea un script de shell que compila y ejecuta el código Java.
     */
    private String crearScriptEjecucion(String javaSource) {
        // Escribir el código fuente en un archivo temporal
        String sourceFile = "/tmp/Main.java";
        String classFile = "/tmp/Main.class";

        // Escape the source for shell insertion
        String escapedSource = javaSource.replace("'", "'\\''");

        return String.join("\n",
                "set -e",
                "echo 'Compilando código Java...'",
                String.format("javac -d /tmp %s", sourceFile),
                String.format("java -cp /tmp Main %s", escapeArgs(javaSource)),
                String.format("timeout %s java -cp /tmp Main", timeoutSegundos),
                "echo 'FIN DE EJECUCIÓN'"
        );
    }

    /**
     * Obtiene los resultados del contenedor en ejecución.
     */
    private ExecutionResult obtenerResultadoContenedor(String containerId, int timeoutSegundos)
            throws IOException, InterruptedException {

        // Esperar a que el contenedor termine
        WaitResponse waitResponse = dockerClient.waitContainerCmd(containerId).withTimeout(timeoutSegundos).exec();

        int exitCode = waitResponse.getStatusCode();

        // Capturar stdout y stderr
        String stdout = capturarSalidaContenedor(containerId, "stdout");
        String stderr = capturarSalidaContenedor(containerId, "stderr");

        return new ExecutionResult(stdout, stderr, exitCode);
    }

    /**
     * Captura la salida de un contenedor.
     */
    private String capturarSalidaContenedor(String containerId, String tipoSalida)
            throws IOException, InterruptedException {

        // Implementación simplificada: obtener logs del contenedor
        // En una implementación completa, usaríamos la API de logs con seguimiento
        StringBuilder resultado = new StringBuilder();

        // Usar exec command para capturar salida en tiempo real
        // Esto es una simplificación; en producción se usaría el endpoint de logs
        com.github.docker.java.client.model.Frame frame;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Bucle para capturar toda la salida
        boolean running = true;
        while (running) {
            // Obtener frames de la salida del contenedor
            // Nota: Esto requiere una implementación más completa con InputStreamMonitor
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Devolver resultado vacío por ahora - implementación completa requeriría
        // monitoreo continuo de los streams del contenedor
        return resultado.toString();
    }

    /**
     * Limpia el contenedor después de la ejecución.
     */
    private void limpiarContenedor() {
        // El contenedor se configura con autoRemove=true,
        // por lo que se eliminará automáticamente al finalizar
        // Este método está aquí por si se necesita una limpieza explícita
    }

    /**
     * Escapa argumentos para línea de comandos shell.
     */
    private String escapeArgs(String input) {
        if (input == null) return "";
        return input.replace(" ", "\\ ").replace("&", "\\&").replace("|", "\\|");
    }

    /**
     * Clase interna para el resultado de ejecución.
     */
    public static class ExecutionResult {
        private final String stdout;
        private final String stderr;
        private final int exitCode;
        private final boolean success;

        public ExecutionResult(String stdout, String stderr, int exitCode) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
            this.success = exitCode == 0;
        }

        public static ExecutionResult error(String mensaje, int codigoSalida) {
            return new ExecutionResult(null, mensaje, codigoSalida);
        }

        public static ExecutionResult runtimeError(String mensaje) {
            return new ExecutionResult(null, mensaje, 1);
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public int getExitCode() {
            return exitCode;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}