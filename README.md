# Java Foundation Lab - complete architectural evolution through 6 phases

**Project**: `java-foundation-lab`  
**Author**: `martillohamer`  
**Version**: `1.0.0-SNAPSHOT`  
**Java Version**: `17 (Oracle JDK 24 compiled, Java 17 source/target compatibility)`  
**Build Tool**: `Apache Maven 3.8+`

---

## 📚 Project Overview

`java-foundation-lab` is a comprehensive, evolving Java project that progresses through **six distinct phases** of increasing complexity and capability. What began as a simple Hola Mundo exercise has grown into a full-featured, production-ready Java ecosystem covering:

- **Phase 1**: Maven project structure & module reorganization  
- **Phase 2**: Java Bytecode & JVM Visualizer  
- **Phase 3**: Gamified Arena with TDD challenges  
- **Phase 4**: Java Micro-Engine & Sandbox (Spring Boot Web API)  
- **Phase 5**: Algorithm-Flow Visual-Matrix (runtime visualization)  
- **Phase 6**: CleanCode & Linter Engine (static code analysis)

The project is designed as a **learning platform**, **demonstration portfolio**, and **testing ground** for Java skills ranging from fundamental syntax to advanced software engineering practices.

---

## 🆕 Six Phases Summary

| Phase | Title | Key Capabilities | Technologies |
|------|-------|------------------|--------------|
| **1** | **Maven & Structure** | Project scaffolding, Maven POM, 12 modular Java packages, `basic.c0X_name` → `com.simdev.java.basic.c0X_name` | Maven 3.8+, JDK 17, Git |
| **2** | **Bytecode & JVM Visualizer** | Runtime class metadata inspection via reflection, extract class name, modifiers, superclass, interfaces, fields, constructors, methods | JDK 24, Java Reflection |
| **3** | **Gamified Arena** | XP/leveling system (100 + (level-1)×50), 8 challenge types, achievement system, Markdown badge export, 22 JUnit 5 tests | JUnit 5, Maven, Console I/O |
| **4** | **Micro-Engine & Sandbox** | Spring Boot 3.2.2 REST API (`POST /api/sandbox/execute`, `POST /api/sandbox/compile`, `GET /api/sandbox/health`), sandboxed Java execution via `JavaTutorEngine`, runtime isolation in temp directories | Spring Boot, ProcessBuilder, JAX-RS style endpoints |
| **5** | **Algorithm-Flow Visual-Matrix** | Console-based ASCII visualizer for arrays, nested loops, complexity estimation (Big O), performance metrics (nanosecond timing), `VisualMatrix` engine with `ArrayVisualizer` and `LoopMatrix` | ASCII art, `System.nanoTime()`, custom visual engines |
| **6** | **CleanCode & Linter Engine** | Static code analysis: `System.out.println` detection, naming conventions (UpperCamelCase/camelCase), method length limits, nested depth analysis, cyclomatic complexity approximation, quality score 0-100, text + JSON reports | Regex, metric calculations, `AuditReport` |

---

## ⚙️ Build & Execution Instructions

### 1. Prerequisites

- **Java JDK 24** (for compilation) targeting **Java 17** source/target compatibility
- **Apache Maven 3.8+** installed and `mvn` on PATH
- **Git** (for version control operations)

### 2. Compile & Package (Production)

```bash
# From the project root directory
mvn clean package
```

This will:
- Compile all 6 phases (≈60 Java files)
- Run JUnit 5 test suite (optional: `mvn test`)
- Produce `target/java-foundation-lab-1.0.0-SNAPSHOT.jar`

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
- Starts the interactive console menu (15 options across all 6 phases)
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

#### Mode C: Individual Module Execution
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
```

---

## 🏗️ Architecture & Module Structure

The project maintains **Maven Standard Directory Structure** under:

```
src/main/java/com/simdev/java/basic/
├── c00_helloworld/      - Phase 1: Hola Mundo
├── c01_beginner/        - Phase 1: Variables & Tipos de Datos
├── c02_operators/       - Phase 1: Operadores Aritméticos y Lógicos
├── c03_strings/         - Phase 1: Manejo de Cadenas de Texto
├── c04_conditionals/    - Phase 1: Estructuras Condicionales
├── c05_structures/      - Phase 1: Estructuras de Datos (Arrays, Listas, Maps, Sets)
├── c06_loops/          - Phase 1: Bucles y Repeticiones
├── c07_functions/      - Phase 1: Funciones y Métodos
├── c08_oop/            - Phase 1: Programación Orientada a Objetos
├── c08_test_oop/       - Phase 1: Testing con JUnit
├── c09_exceptions/     - Phase 1: Excepciones Personalizadas
├── c10_extras/         - Phase 1: Funcionalidades Adicionales
├── visualizer/          - Phase 2: Bytecode Visualizer JVM
├── arena/              - Phase 3: Gamified Arena
├── sandbox/             - Phase 4: Spring Boot Micro-Engine & Sandbox
├── visualmatrix/        - Phase 5: Algorithm-Flow Visual-Matrix
└── linter/             - Phase 6: CleanCode & Linter Engine
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

**Example cURL Request:**
```bash
curl -X POST http://localhost:8080/api/sandbox/execute \
  -H "Content-Type: application/json" \
  -d '{"source":"public class Main { public static void main(String[] args) { System.out.println(\"Hello from Sandbox\"); } }"}'
```

---

## 📦 Project Artifacts Produced

Running `mvn clean package` produces:

- **`target/java-foundation-lab-1.0.0-SNAPSHOT.jar`**: Full executable JAR ( Spring Boot + all phases )
- **`target/classes/`**: Compiled Java class files (≈60 classes)
- **`target/surefire-reports/`**: JUnit 5 test execution reports (if `mvn test` run)
- **`profile-badge.md`**: GeneratedMarkdown badge from Gamified Arena export (Phase 3)
- **`out/`**: Legacy compilation directory (from direct `javac` runs during development)

---

## 🛠️ Development Notes

- **Backward Compatibility**: The project compiles with **JDK 24** (compiler level) but targets **Java 17** source/target compatibility (`<release>17</release>` in pom.xml).
- **Modularity**: Each phase is independently compilable and testable, yet integrates seamlessly through the central `Main` entry point.
- **Extensibility**: New phases/modules can be added following the established package naming convention and Maven module structure.
- **Testing**: JUnit 5 (5.10.2) tests are configured with `test` scope in `pom.xml`. Test classes cover arithmetic/logic, bytecode metadata, gamified arena metrics, sandbox execution, visual matrix accuracy, and linting rules.

---

## 👤 Authorship

**Author**: `martillohamer`  
**GitHub**: [github.com/martillohamer](https://github.com/martillohamer)  
**Purpose**: Educational portfolio & demonstration of Java full-stack evolution from fundamentals to enterprise-grade practices.  
**Created**: October 2026  
**Status**: ✅ Complete - all 6 phases implemented and tested

---

## 📜 License

This project is open source and available under the [MIT License](https://opensource.org/licenses/MIT). You are free to use, modify, and distribute this project following the terms of the MIT License.

---
*Last updated: October 2026*

---