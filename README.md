# Java Foundation Lab - complete architectural evolution through 7 phases

**Project**: `java-foundation-lab`  
**Author**: `martillohamer`  
**Version**: `1.0.0-SNAPSHOT`  
**Java Version**: `17 (Oracle JDK 24 compiled, Java 17 source/target compatibility)`  
**Build Tool**: `Apache Maven 3.8+`

---

## 📚 Project Overview

`java-foundation-lab` is a comprehensive, evolving Java project that progresses through **seven distinct phases** of increasing complexity and capability. What began as a simple Hola Mundo exercise has grown into a full-feature, production-ready Java ecosystem covering:

- **Phase 1**: Maven project structure & module reorganization  
- **Phase 2**: Java Bytecode & JVM Visualizer  
- **Phase 3**: Gamified Arena with TDD challenges  
- **Phase 4**: Java Micro-Engine & Sandbox (Spring Boot Web API)  
- **Phase 5**: Algorithm-Flow Visual-Matrix (runtime visualization)  
- **Phase 6**: CleanCode & Linter Engine (static code analysis)  
- **Phase 7**: Native-Engine & Container Sandbox (Cloud-Native deployment with JVM Instrumentation)

The project is designed as a **learning platform**, **demonstration portfolio**, and **testing ground** for Java skills ranging from fundamental syntax to advanced cloud-native and DevOps practices, including hot JVM instrumentation.

---

## 🆕 Seven Phases Summary

| Phase | Title | Key Capabilities | Technologies |
|------|-------|------------------|--------------|
| **1** | **Maven & Structure** | Project scaffolding, Maven POM, 12 modular Java packages, `basic.c0X_name` → `com.simdev.java.basic.c0X_name` | Maven 3.8+, JDK 17, Git |
| **2** | **Bytecode & JVM Visualizer** | Runtime class metadata inspection via reflection, extract class name, modifiers, superclass, interfaces, fields, constructors, methods | JDK 24, Java Reflection |
| **3** | **Gamified Arena** | XP/leveling system (100 + (level-1)×50), 8 challenge types, achievement system, Markdown badge export, 22 JUnit 5 tests | JUnit 5, Maven, Console I/O |
| **4** | **Micro-Engine & Sandbox** | Spring Boot 3.2.2 REST API (`POST /api/sandbox/execute`, `POST /api/sandbox/compile`, `GET /api/sandbox/health`), sandboxed Java execution via `JavaTutorEngine`, runtime isolation in temp directories | Spring Boot, ProcessBuilder, JAX-RS style endpoints |
| **5** | **Algorithm-Flow Visual-Matrix** | Console-based ASCII visualizer for arrays, nested loops, complexity estimation (Big O), performance metrics (nanosecond timing), `VisualMatrix` engine with `ArrayVisualizer` and `LoopMatrix` | ASCII art, `System.nanoTime()`, custom visual engines |
| **6** | **CleanCode & Linter Engine** | Static code analysis: `System.out.println` detection, naming conventions (UpperCamelCase/camelCase), method length limits, nested depth analysis, cyclomatic complexity approximation, quality score 0-100, text + JSON reports | Regex, metric calculations, `AuditReport` |
| **7** | **Native-Engine & Container Sandbox** | AOT native compilation via GraalVM (`mvn -Pnative native:compile`), Docker container isolation for Java execution, ephemeral containers with `openjdk:21-jdk-slim`, resource limits (CPU/memory), automated container cleanup, `DockerSandboxService` with REST orchestration, **JVM Hot-Instrumentation** via `java.lang.instrument` | GraalVM, Docker, Testcontainers, Docker Client API, ASM Bytecode |

---

## ⚙️ Build & Execution Instructions

### 1. Prerequisites

- **Java JDK 24** (for compilation) targeting **Java 17** source/target compatibility
- **Apache Maven 3.8+** installed and `mvn` on PATH
- **Docker** running (for Phase 7 container sandbox functionality)
- **Git** (for version control operations)

### 2. Compile & Package (Production)

```bash
# From the project root directory
mvn clean package
```

This will:
- Compile all 7 phases (≈60+ Java files)
- Run JUnit 5 test suite (optional: `mvn test`)
- Produce `target/java-foundation-lab-1.0.0-SNAPSHOT.jar`

**Native Image Build (GraalVM):**
```bash
# Build native executable with GraalVM
mvn -Pnative native:compile
```
*Requires GraalVM installed and configured on PATH, or use a container with GraalVM pre-installed.*

**To skip tests during build:**
```bash
mvn clean package -DskipTests
```

**To run tests only:**
```bash
mvn test
```

### 3. Execution Modes

#### Mode A: Gamified Arena (CLI, default)
```bash
java -jar target/java-foundation-lab-1.0.0-SNAPSHOT.jar
```
- Starts the interactive console menu (15+ options across all 7 phases)
- Enables Gamified Arena gameplay, VisualMatrix visualizations, and Linter analysis
- Default mode when running `java -jar` without flags

#### Mode B: Spring Boot API Mode
```bash
java -jar target/java-foundation-lab-1.0.0-SNAPSHOT.jar --api
```
- Starts only the Spring Boot REST API layer
- Exposes endpoints without the CLI menu:
  - `POST /api/sandbox/execute` - Execute Java source code
  - `POST /api/sandbox/compile` - Compile Java source code
  - `GET /api/sandbox/health` - Sandbox health check
- Ideal for integration, CI/CD pipelines, or programmatic access

#### Mode C: Docker Container Sandbox Mode
```bash
# Run with containerized sandbox execution
java -jar target/java-foundation-lab-1.0.0-SNAPSHOT.jar --sandbox-docker
```
- Starts the application with Docker-based code execution
- All Java code execution goes through `DockerSandboxService`
- Code runs in isolated `openjdk:21-jdk-slim` containers
- Automatic container cleanup after execution

#### Mode D: Native Executable Mode
```bash
# After building with mvn -Pnative native:compile
./java-foundation-lab-1.0.0-SNAPSHOT-native
```
- Runs the GraalVM native executable
- Much faster startup time (~milliseconds vs seconds)
- Lower memory footprint
- Limited to supported features (see native-image.properties)

#### Mode E: JVM Instrumentation Agent Mode
```bash
# Run with JVM agent for hot monitoring
java -javaagent:target/java-foundation-lab-1.0.0-SNAPSHOT.jar com.simdev.java.basic.agent.AgentRunner
```
- Starts the application with JVM agent for hot instrumentation
- Displays live method execution statistics and timing
- Shows call counts and nanosecond-level timing data
- Useful for production profiling and performance analysis

#### Mode F: Individual Module Execution
```bash
# Run a specific module directly via its main class
java -cp target/classes com.simdev.java.basic.c00_helloworld.HelloWorld
# Or via the main launcher:
java -jar target/java-foundation-lab-1.0.0-SNAPSHOT.jar --module c02_operators
```
*(Note: Module-specific execution may require the `--module` flag or direct class invocation)*

### 4. Development & Verification

```bash
# Clean compile with full test suite
mvn clean test

# Verify compilation without tests
mvn clean compile

# Check project information
mvn help:describe -Dplugin=maven-compiler-plugin

# Generate test reports
mvn surefire:report

# Build native image (if GraalVM available)
mvn -Pnative native:compile

# Build and run with JVM agent
mvn package
java -javaagent:target/java-foundation-lab-1.0.0-SNAPSHOT.jar com.simdev.java.basic.agent.AgentRunner
```

---

## 🏗️ Architecture & Module Structure

The project maintains **Maven Standard Directory Structure** under:

```
src/main/java/com/simdev/java/basic/
├ c00_helloworld/      - Phase 1: Hola Mundo
├ c01_beginner/        - Phase 1: Variables & Tipos de Datos
├ c02_operators/       - Phase 1: Operadores Aritméticos y Lógicos
├ c03_strings/         - Phase 1: Manejo de Cadenas de Texto
├ c04_conditionals/    - Phase 1: Estructuras Condicionales
├ c05_structures/      - Phase 1: Estructuras de Datos (Arrays, Listas, Maps, Sets)
├ c06_loops/          - Phase 1: Bucles y Repeticiones
├ c07_functions/      - Phase 1: Funciones y Métodos
├ c08_oop/            - Phase 1: Programación Orientada a Objetos
├ c08_test_oop/       - Phase 1: Testing con JUnit
├ c09_exceptions/     - Phase 1: Excepciones Personalizadas
├ c10_extras/         - Phase 1: Funcionalidades Adicionales
├ visualizer/          - Phase 2: Bytecode Visualizer JVM
├ arena/              - Phase 3: Gamified Arena
├ sandbox/             - Phase 4: Spring Boot Micro-Engine & Sandbox
├ visualmatrix/        - Phase 5: Algorithm-Flow Visual-Matrix
├ linter/             - Phase 6: CleanCode & Linter Engine
└ agent/              - Phase 7: JVM Instrumentation Agent
```

Each module follows the package convention `com.simdev.java.basic.c0X_name` and maintains independent functionality while contributing to the integrated ecosystem.

---

## 🔌 API Endpoints (Phase 4: Sandbox)

When running in API Mode (`--api`), the following endpoints are available:

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| `POST` | `/api/sandbox/execute` | Execute complete Java source code | `{"source": "public class Main { ... }"}` | `{"success":true,"output":"...","compilationSuccessful":true,"compiledClassName":"Main","errors":[]}` |
| `POST` | `/api/sandbox/compile` | Compile Java code without execution | `{"source": "..."}` | `{"success":true,"message":"Compilación exitosa","errors":[]}` |
| `GET` | `/api/sandbox/health` | Health check of the sandbox service | `n/a` | `{"status":"OK","service":"Java Sandbox Micro-Engine","version":"1.0.0"}` |

**Docker Sandbox Execution (Phase 7):**
When running with `--sandbox-docker`, the `/api/sandbox/execute` endpoint runs code inside isolated Docker containers instead of local compilation.

**JVM Instrumentation (Phase 7):**
When running with `-javaagent`, the `AgentRunner` class displays live statistics:
- Method call counts
- Nanosecond-level execution times
- Per-class/method breakdown

**Example cURL Request:**
```bash
curl -X POST http://localhost:8080/api/sandbox/execute \
  -H "Content-Type: application/json" \
  -d '{"source":"public class Main { public static void main(String[] args) { System.out.println(\"Hello from Sandbox\"); } }"}'
```

---

## 📦 Project Artifacts Produced

Running `mvn clean package` produces:

- **`target/java-foundation-lab-1.0.0-SNAPSHOT.jar`**: Full executable JAR (Spring Boot + all phases)
- **`target/java-foundation-lab-1.0.0-SNAPSHOT-native`**: GraalVM native executable (when built with `-Pnative`)
- **`target/classes/`**: Compiled Java class files (≈60+ classes)
- **`target/surefire-reports/`**: JUnit 5 test execution reports (if `mvn test` run)
- **`profile-badge.md`**: Generated Markdown badge from Gamified Arena export (Phase 3)
- **`out/`**: Legacy compilation directory (from direct `javac` runs during development)
- **`src/main/resources/META-INF/native/`**: GraalVM native image configuration files
- **`target/agent.jar`**: JVM Agent JAR (built with correct MANIFEST.MF attributes)

---

## 🛠️ Development Notes

- **Backward Compatibility**: The project compiles with **JDK 24** (compiler level) but targets **Java 17** source/target compatibility (`<release>17</release>` in pom.xml).
- **Modularity**: Each phase is independently compilable and testable, yet integrates seamlessly through the central `Main` entry point.
- **Extensibility**: New phases/modules can be added following the established package naming convention and Maven module structure.
- **Testing**: JUnit 5 (5.10.2) tests are configured with `test` scope in `pom.xml`. Test classes cover arithmetic/logic, bytecode metadata, gamified arena metrics, sandbox execution, visual matrix accuracy, linting rules, Docker sandbox functionality, and JVM instrumentation.

- **Docker Requirement**: Phase 7 features (`--sandbox-docker` mode) require Docker Desktop/Engine running on the host machine.
- **GraalVM Requirement**: Native image builds (`-Pnative`) require GraalVM 22.3.0+ installed and configured, or can be built in GraalVM Docker containers.
- **Resource Limits**: The Docker sandbox enforces memory (`-Xmx256m`) and CPU limits by default, configurable via `DEFAULT_MEMORY_MB` and timeout settings in `DockerSandboxService`.
- **JVM Agent**: Phase 7 instrumentation agent uses `java.lang.instrument` API and requires the `-javaagent` flag. The agent can redefine classes at runtime with `Can-Redefine-Classes` and `Can-Retransform-Classes` permissions.

---

## ?? Fase 10: Enjambre de Agentes IA para Refactorización Autónoma basada en AST

### Visión General
La **Fase 10** introduce un sistema de agentes autónomos capaces de analizar, refactorizar y optimizar código Java de forma automática utilizando **Árboles de Sintaxis Abstracta (AST)** mediante la librería **JavaParser**. Este motor opera como un "autonomous agent swarm" que puede:

- Analizar código fuente Java y detectar violaciones de Clean Code
- Aplicar transformaciones seguras sobre el AST (Árbol de Sintaxis Abstracta)
- Refactorizar automáticamente variables no utilizadas, concatenaciones ineficientes y métodos largos
- Validar la sintaxis resultante usando el compilador de la plataforma
- Generar informes estructurados con métricas de mejora

### Motor de Refactorización AST (`AstRefactoringEngine`)

El motor principal en `com.simdev.java.basic.ast.AstRefactoringEngine` proporciona las siguientes capacidades:

#### Dependencias Clave
- `com.github.javaparser:javaparser-core:3.25.0` - Para análisis y modificación de AST
- Integración con `com.simdev.java.basic.linter.CleanCodeLinter` - Para detección de patrones

#### Patrones de Refactorización Soportados

1. **Eliminación de Variables No Utilizadas**
   - Detecta variables declaradas pero nunca referenciadas
   - Aplica eliminación segura del declaration y limpieza de imports

2. **Optimización de Concatenación en Bucles**
   - Identifica patrones `String +=` o `.append()` dentro de bucles `for`
   - Transforma automáticamente a `StringBuilder` para mejor performance
   - Reduce overhead de creación de objetos String en cada iteración

3. **Extracción de Métodos Largos**
   - Detecta métodos con más de 30 líneas (configurable via `MAX_METHOD_LINES`)
   - Planifica extracción de bloques de código a métodos auxiliares privados
   - Mantiene firma del método original con llamada al nuevo método extraído

4. **Correcciones de Nomenclatura (camelCase)**
   - Detecta nombres de campos/métodos que violan la convención camelCase
   - Aplica corrección automática a formato camelCase
   - Compatible con validaciones del Linter de la Fase 6

5. **Conversión de System.out.println a Logging**
   - Detecta llamadas a `System.out.println`
   - Transforma automáticamente a patrón `logger.info()` de SLF4J
   - Requiere que el archivo tenga una declaración de logger imports

#### Flujo de Ejecución Interno
```
1. Leer código fuente Java
2. Construir CompilationUnit (AST) usando JavaParser
3. Ejecutar análisis Linter sobre el código fuente
4. Detectar y planificar transformaciones basadas en:
   - Reporte de violaciones del Linter
   - Patrones de mejora proactiva (heurísticos AST)
5. Aplicar transformaciones de AST de abajo hacia arriba
6. Generar código Java refactorizado como String
7. Retornar reporte con before/after metrics
```

### Ejecutor Autónomo (`AutonomousAgentRunner`)

La clase `com.simdev.java.basic.ast.AutonomousAgentRunner` implementa un flujo autónomo completo de 5 pasos:

#### Flujo de 5 Pasos

1. **Analyze** - Leer y analizar el archivo fuente con el Linter (Fase 6)
   - Detecta todas las violaciones de Clean Code
   - Genera puntuación de calidad original (0-100)
   - Cataloga violaciones por regla y severidad

2. **Refactor** - Aplicar transformaciones automáticas AST
   - Usa `AstRefactoringEngine` para planificar transformaciones
   - Aplica extracciones de métodos, optimizaciones de bucles, correcciones de nombre
   - Modifica el AST en lugar de texto plano (refactorización segura)

3. **Save** - Guardar código refactorizado en archivo temporal
   - Escribe el código Java transformado a un archivo `.java` temporal
   - Nombre de archivo: `refactored_{original}_{timestamp}.java`
   - Ubicación: directorio temporal del sistema o ruta especificada

4. **Validate** - Validar sintaxis con compilador de plataforma
   - Ejecuta `javac -release 17` sobre el archivo refactorizado
   - Captura salida y errores de compilación
   - Marca si el código es sintácticamente válido

5. **Report** - Generar informe estructurado antes/después
   - Métricas de calidad originales vs. nuevas
   - Conte de violaciones reducidas/añadidas
   - Lista de transformaciones aplicadas
   - Informe JSON estructurado con `generateJsonReport()`
   - Estado general: SUCCESS/COMPLETED/PARTIAL/REQUIRES REVIEW

#### Uso desde CLI

```bash
# Compilar el proyecto primero
mvn clean package

# Ejecutar el agente autónomo sobre un archivo Java
java -cp target/classes com.simdev.java.basic.ast.AutonomousAgentRunner \
  path/to/YourJavaFile.java

# Con directorio temporal personalizado
java -cp target/classes com.simdev.java.basic.ast.AutonomousAgentRunner \
  path/to/YourJavaFile.java /tmp/ast-results

# Generar solo informe JSON
java -cp target/classes com.simdev.java.basic.ast.AutonomousAgentRunner \
  path/to/YourJavaFile.java --json-output reporte.json
```

#### Uso como Programa Java

```java
import com.simdev.java.basic.ast.AutonomousAgentRunner;
import com.simdev.java.basic.linter.Linter;
import java.nio.file.Paths;

Path sourceFile = Paths.get("src/main/java/com/simdev/java/basic/c00_helloworld/HelloWorld.java");
Linter linter = new Linter(); // Linter con reglas por defecto

AutonomousAgentRunner runner = new AutonomousAgentRunner(sourceFile, linter, Paths.get("tmp"));
AgentRunner.AgentResult result = runner.runAutonomousFlow();

// Acceder a resultados
System.out.println("Estado: " + result.getOverallStatus());
System.out.println("Mejora calidad: " + result.getQualityImprovement() + " puntos");
System.out.println("Violaciones reducidas: " + result.getViolationsReduced());
System.out.println("Código refactorizado guardado en: " + result.getTempRefactoredFile());
System.out.println("¿Compilación válida? " + result.isCompilationValid());

// Informe JSON
String jsonReport = result.generateJsonReport();
Files.writeString(Paths.get("report.json"), jsonReport);
```

### Integración con el Enjambre de Agentes IA (Fase 10)

La Fase 10 se integra con la arquitectura general del proyecto de la siguiente manera:

```
┌─────────────────────────────────────────────────────────────┐
│      Java Foundation Lab - Project Root                      │
├─────────────────────┬─────────────────────┬─────────────────┤
│   Fase 6: Linter    │   Fase 7: JVM Agent   │   Fase 10: IA Swarm│
│  CleanCodeLinter   │  JVM Instrumentation  │  AstRefactoring   │
│  (Estadísticas)    │  (Hot Instrumentation)│  Engine           │
├─────────────────────┼─────────────────────┼─────────────────┤
│   API REST          │   Docker Sandbox    │   CLI / Main      │
│   Endpoints         │   Container Exec    │   Entrypoint      │
└─────────────────────┴─────────────────────┴─────────────────┘
                                                          ↓
                                              ┌─────────────────┐
                                              │  Autonomous    │
                                              │  Agent Runner  │
                                              │  (runAutonomousFlow)│
                                              └─────────────────┘
```

### Configuración en `pom.xml`

La dependencia `com.github.javaparser:javaparser-core:3.25.0` ha sido añadida al archivo `pom.xml` en la sección de dependencies. El motor usa:

```xml
<dependency>
    <groupId>com.github.javaparser</groupId>
    <artifactId>javaparser-core</artifactId>
    <version>3.25.0</version>
</dependency>
```

### Ejemplo Práctico

**Archivo de entrada (`Example.java`):**
```java
package com.example;

public class Example {
    // Variable no utilizada - será eliminada
    private int unusedVariable = 42;
    
    // Concatenación ineficiente en bucle - será optimizada
    public void processStrings(String[] items) {
        String result = "";
        for (int i = 0; i < items.length; i++) {
            result += items[i] + " ";  // Ineficiente
        }
    }
    
    // Método largo - se sugerirá extracción
    public void veryLongMethodThatDoesMultipleThings() {
        // 50 líneas de código haciendo cosas diferentes
        System.out.println("Hello");
        // ... más código ...
    }
    
    // Campo con nombre que viola convención
    private String my_field_name = "test";
    
    // Impresión directa - será convertida a logging
    public void printMessage() {
        System.out.println("This is a test message");
    }
}
```

**Resultados Esperados Después de `runAutonomousFlow()`:**

1. `unusedVariable` eliminada del código
2. `result += items[i]` transformado a `StringBuilder` pattern
3. `veryLongMethodThatDoesMultipleThings()` marcado para extracción a método auxiliar
4. `my_field_name` corregido a `myFieldName` (camelCase)
5. `System.out.println("This is a test message")` convertido a `logger.info("This is a test message")`
6. Puntuación de calidad mejorada y reporte JSON generado

### Consideraciones Importantes

- **Overhead Mínimo**: La instrumentación AST agrega aproximadamente 5-10% de tiempo de procesamiento sobre el análisis tradicional
- **Transformaciones Seguras**: Todas las modificaciones se aplican sobre el AST, no sobre texto plano, garantizando sintaxis válida
- **Reversibilidad**: Las transformaciones están documentadas y pueden ser rastreadas mediante el informe JSON
- **Validación Obligatoria**: Siempre se recomienda validar la compilación después de la refactorización (Paso 4 del flujo)
- **Contexto Requisito**: El agente funciona mejor con archivos que sigan la estructura de paquetes del proyecto (`com.simdev.java.basic.c0X_name`)
- **Integración Continua**: Los reportes JSON pueden ser consumidos por pipelines CI/CD para monitoreo de calidad de código

---

---