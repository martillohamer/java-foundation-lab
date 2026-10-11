package com.simdev.java.basic.agent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Clase de demostración interactiva para el Agente JVM.
 * <p>
 * Esta clase sirve como punto de entrada para verificar que el agente
 * está funcionando correctamente. Se utiliza típicamente pasando la
 * bandera JVM: <code>-javaagent:path/to/agent.jar</code>
 * al iniciar cualquier aplicación Java.
 * </p>
 * <p>
 * Ejemplo de uso:
 * <pre>
 * java -javaagent:target/agent.jar -cp target/classes com.simdev.java.basic.agent.AgentRunner
 * </pre>
 * </p>
 */
public class AgentRunner {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  INSTRUMENTACIÓN JVM EN CALIENTE");
        System.out.println("==========================================");
        System.out.println();

        // Verificar si el agente ya está activo comprobando
        // si las estadísticas existen (cargadas premain)
        List<Agent.MethodStats> stats = Agent.getMethodStats();

        if (stats.isEmpty()) {
            System.out.println("⚠  Agente JVM no detectado.");
            System.out.println("   Ejecute con: java -javaagent:agent.jar ...");
            System.out.println();
            System.out.println("Características disponibles cuando el agente está activo:");
            System.out.println("  - Monitoreo de llamadas a métodos");
            System.out.println("  - Medición de tiempo en nanosegundos");
            System.out.println("  - Estadísticas por clase/método");
            System.out.println("  - Reinicio de estadísticas");
            System.out.println();
        } else {
            System.out.println("✅ Agente JVM activo - Monitoreo en progreso");
            System.out.println();
            System.out.println("Estadísticas recopiladas:");
            System.out.println("-------------------------");

            for (Agent.MethodStats stat : stats) {
                System.out.println(stat);
            }

            System.out.println("-------------------------");
            System.out.println();
            System.out.println("Comandos disponibles:");
            System.out.println("  'r' - Reiniciar estadísticas");
            System.out.println("  'q' - Salir");
            System.out.println();

            // Ejemplo de actividad simulada
            demostrarActividad(stats);
        }

        System.out.println();
        System.out.println("Presione Ctrl+C para salir.");
    }

    /**
     * Demuestra actividad de método simulada para mostrar el monitoreo.
     */
    private static void demostrarActividad(List<Agent.MethodStats> stats) {
        // Simular algunas llamadas a métodos para demostrar el seguimiento
        for (int i = 0; i < Math.min(5, stats.size()); i++) {
            Agent.MethodStats stat = stats.get(i);
            stat.incrementCallCount();
            // Simular tiempo aleatorio entre 1000 y 50000 nanosegundos
            long tiempoMuestra = 1000 + (long) (Math.random() * 49000);
            stat.addTotalNanos(tiempoMuestra);
        }

        // Mostrar estadísticas actualizadas
        System.out.println("\nEstadísticas después de actividad simulada:");
        for (Agent.MethodStats stat : stats) {
            System.out.println(stat);
        }
    }

    /**
     * Reinicia todas las estadísticas recopiladas por el agente.
     */
    public static void reiniciarEstadisticas() {
        Agent.resetStats();
        System.out.println("🔄 Estadísticas reiniciadas");
    }
}