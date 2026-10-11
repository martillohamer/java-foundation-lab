package com.simdev.java.basic.rag;

import com.simdev.java.basic.ast.AstRefactoringEngine;
import com.simdev.java.basic.linter.CleanCodeLinter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración Spring para el Motor RAG (Fase 11).
 * <p>
 * Define los beans necesarios para que el {@link RagRestController}
 * y otros componentes puedan inyectar {@link CodeRagEngine} con sus
 * dependencias correctas.
 * </p>
 * <p>
 * Este patrón de configuración permite:
 * - Centralizar la creación de instancias complejas
 * - Inyectar dependencias en el constructor (Constructor Injection)
 * - Facilitar pruebas unitarias y mocks
 * - Mantener el patrón de inversión de dependencias
 * </p>
 */
@Configuration
public class RagConfig {

    /**
     * Crea y configura el {@link VectorStore} que servirá como almacén
     * vectorial semántico indexado.
     * <p>
     * El VectorStore mantiene los embeddings en memoria y provee operaciones
     * de búsqueda por similitud de coseno con filtros por fase y tipo.
     * </p>
     *
     * @return Nueva instancia de VectorStore lista para ser usada
     */
    @Bean
    public VectorStore vectorStore() {
        return new VectorStore();
    }

    /**
     * Crea y configura el {@link CleanCodeLinter} para análisis de calidad
     * de código. Este linter se usa como contexto adicional para el motor
     * RAG al indexar y describir elementos de código.
     * <p>
     * El CleanCodeLinter detecta conveniones de código, nombres de métodos,
     * uso de System.out.println, longitud de métodos, etc.
     * </p>
     *
     * @return Nueva instancia de CleanCodeLinter lista para ser usada
     */
    @Bean
    public CleanCodeLinter cleanCodeLinter() {
        return new CleanCodeLinter();
    }

    /**
     * Crea y configura el {@link AstRefactoringEngine} para refactorización
     * basada en Árboles de Sintaxis Abstracta.
     * <p>
     * Este motor analiza el AST de las clases Java para identificar patrones
     * de código, variables no usadas, concaténaciones de bucles, métodos
     * largos, y otros patrones detectados en las fases 1-10 del proyecto.
     * </p>
     *
     * @return Nueva instancia de AstRefactoringEngine lista para ser usada
     */
    @Bean
    public AstRefactoringEngine astRefactoringEngine() {
        return new AstRefactoringEngine();
    }

    /**
     * Crea y configura el {@link CodeRagEngine} con todas sus dependencias
     * inyectadas.
     * <p>
     * El CodeRagEngine es el núcleo del motor RAG que:
     * - Escanea las clases Java del proyecto (fases 1-11)
     * - Genera embeddings semánticos usando EmbeddingUtils
     * - Indexa los fragmentos en el VectorStore
     * - Provee búsquedas semánticas con filtros por fase y tipo
     * - Retorna estadísticas del índice
     * </p>
     * <p>
     * Este bean recibe por inyección constructor:
     * - {@link VectorStore} para almacenamiento de embeddings
     * - {@link CleanCodeLinter} para análisis de calidad de código
     * - {@link AstRefactoringEngine} para análisis AST adicional
     * </p>
     *
     * @param vectorStore           Almacén vectorial inyectado
     * @param cleanCodeLinter       Linter de calidad inyectado
     * @param astRefactoringEngine  Motor de refactorización AST inyectado
     * @return Nueva instancia de CodeRagEngine completamente configurada
     */
    @Bean
    public CodeRagEngine codeRagEngine(VectorStore vectorStore,
                                       CleanCodeLinter cleanCodeLinter,
                                       AstRefactoringEngine astRefactoringEngine) {
        return new CodeRagEngine(vectorStore, cleanCodeLinter, astRefactoringEngine);
    }
}