package com.simdev.java.basic;

import com.simdev.java.basic.arena.GamifiedArena;
import com.simdev.java.basic.linter.CleanCodeLinter;
import com.simdev.java.basic.sandbox.SandboxController;
import com.simdev.java.basic.visualmatrix.VisualMatrix;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private static final String[] MODULOS = {
            "c00_helloworld - Hola Mundo",
            "c01_beginner - Variables y Tipos de Datos",
            "c02_operators - Operadores Aritméticos y Lógicos",
            "c03_strings - Manejo de Cadenas de Texto",
            "c04_conditionals - Estructuras Condicionales",
            "c05_structures - Estructuras de Datos (Arrays, Listas, Maps, Sets)",
            "c06_loops - Bucles y Repeticiones",
            "c07_functions - Funciones y Métodos",
            "c08_oop - Programación Orientada a Objetos (Clases, Herencia, Polimorfismo)",
            "c08_test_oop - Testing con JUnit",
            "c09_exceptions - Excepciones Personalizadas",
            "c10_extras - Funcionalidades Adicionales",
            "13. Visualizar Bytecode JVM - Inspeccionar metadatos de clase",
            "14. Visualizar Algorithm-Flow Matrix - Ejecución y estructuras de datos",
            "15. Auditoría CleanCode & Linter Estático"
    };

    private static final String[] SANDBOX_ENDPOINTS = {
            "POST   /api/sandbox/execute - Ejecutar código Java",
            "POST   /api/sandbox/compile   - Compilar código Java",
            "GET    /api/sandbox/health    - Estado del sandbox"
    };

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Verificar modo de inicio: si hay argumentos de Spring para modo API, iniciar solo endpoints
        boolean apiMode = args.length > 0 && args[0] != null && args[0].equalsIgnoreCase("--api");
        
        if (apiMode) {
            System.out.println("\n=== Spring Boot Sandbox API Mode ===");
            System.out.println("La aplicación está corriendo en modo API REST.");
            System.out.println("Endpoints disponibles:");
            for (String endpoint : SANDBOX_ENDPOINTS) {
                System.out.println("  " + endpoint);
            }
            System.out.println("Use 'curl' o Postman para probar los endpoints.\n");
            return;
        }

        // Modo por defecto: Mostrar menú principal de la Gamified Arena
        System.out.println("\n" + "=".repeat(60));
        System.out.println("   JAVA CLI GAMIFIED ARENA - VERSION PRODUCCIÓN");
        System.out.println("=".repeat(60) + "\n");
        
        GamifiedArena arena = new GamifiedArena("Programador");
        arena.iniciar();

        // Después de salir de la arena, mostrar información del linter
        System.out.println("\n" + "=".repeat(60));
        System.out.println("   LINTING & CLEAN CODE ANALYSIS");
        System.out.println("=".repeat(60) + "\n");
        
        System.out.println("El motor CleanCode & Linter Engine (Fase 6) está integrado en el proyecto.");
        System.out.println("Ejecute 'mvn clean test' para correr las pruebas de linting, o visite los endpoints API.");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("   ¡Gracias por jugar! Hasta la próxima, Programador.");
        System.out.println("=".repeat(60) + "\n");
    }
}