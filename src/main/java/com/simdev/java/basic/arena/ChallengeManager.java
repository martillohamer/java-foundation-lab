package com.simdev.java.basic.arena;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestor de desafíos para la Gamified Arena.
 * Define desafíos asociados a los módulos Java y evalúa si el jugador
 * los completa exitosamente basándose en su conocimiento.
 */
public class ChallengeManager {

    // Tipo de desafío
    public enum TipoDesafio {
        OPERADORES,
        CADENAS,
        CONDICIONALES,
        ESTRUCTURAS_DATOS,
        LOOPS,
        FUNCIONES,
        OOP,
        EXCEPCIONES
    }

    // Clase interna pública para representar un desafío
    public static class Desafio {
        private String id;
        private String titulo;
        private String descripcion;
        private TipoDesafio tipo;
        private int puntosXP;
        private String codigoReferencia; // Código de ejemplo que debe entender/manipular
        private boolean completado;
        private int intentos;
        private double porcentajeExito; // Basado en intentos previos

        public Desafio(String id, String titulo, String descripcion, TipoDesafio tipo, int puntosXP,
                       String codigoReferencia) {
            this.id = id;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.tipo = tipo;
            this.puntosXP = puntosXP;
            this.codigoReferencia = codigoReferencia;
            this.completado = false;
            this.intentos = 0;
            this.porcentajeExito = 0.0;
        }

        // Getters y Setters públicos completos
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public TipoDesafio getTipo() { return tipo; }
        public void setTipo(TipoDesafio tipo) { this.tipo = tipo; }

        public int getPuntosXP() { return puntosXP; }
        public void setPuntosXP(int puntosXP) { this.puntosXP = puntosXP; }

        public String getCodigoReferencia() { return codigoReferencia; }
        public void setCodigoReferencia(String codigoReferencia) { this.codigoReferencia = codigoReferencia; }

        public boolean isCompletado() { return completado; }
        public void setCompletado(boolean completado) { this.completado = completado; }

        public int getIntentos() { return intentos; }
        public void incrementarIntentos() { this.intentos++; }

        public double getPorcentajeExito() { return porcentajeExito; }
        public void setPorcentajeExito(double porcentajeExito) { this.porcentajeExito = porcentajeExito; }
    }

    // Colección de desafíos disponibles (lista pública para acceso controlado)
    private List<Desafio> desafios;
    private Random random;

    public ChallengeManager() {
        this.desafios = new ArrayList<>();
        this.random = new Random();
        // Inicializar desafíos por defecto
        inicializarDesafios();
    }

    /**
     * Inicializa la colección de desafíos asociados a los módulos Java.
     */
    private void inicializarDesafios() {
        // Desafíos de Operadores
        desafios.add(new Desafio("OP-001", "Maestro de Operadores",
                "Dado dos enteros a=12 y b=5, calcular el resultado de a % b y a >> 1",
                TipoDesafio.OPERADORES, 50, "int a = 12, b = 5;"));

        desafios.add(new Desafio("OP-002", "Lógica Booleana Avanzada",
                "Simplificar el expression: (true && false) || (false && true)",
                TipoDesafio.OPERADORES, 75, "boolean x = true && false || false && true;"));

        // Desafíos de Cadenas
        desafios.add(new Desafio("STR-001", "Manipulador de Cadenas",
                "Dada la cadena \"Hello World\", obtener \"World\" usando substring",
                TipoDesafio.CADENAS, 60, "String s = \"Hello World\";"));

        // Desafíos de Condicionales
        desafios.add(new Desafio("CON-001", "Selector de Rutas",
                "Dado un boolean true, qué rama se ejecuta en un if-else simple",
                TipoDesafio.CONDICIONALES, 65, "if (true) { ... } else { ... }"));

        // Desafíos de Estructuras de Datos
        desafios.add(new Desafio("STRU-001", "Acceso a Array",
                "Acceder al tercer elemento (índice 2) de un array de enteros {10, 20, 30, 40, 50}",
                TipoDesafio.ESTRUCTURAS_DATOS, 70, "int[] arr = {10, 20, 30, 40, 50};"));

        // Desafíos de Loops
        desafios.add(new Desafio("LOOP-001", "Contador de Bucles",
                "Cuántas iteraciones ejecuta un bucle for classic: for(int i=0; i<5; i++)",
                TipoDesafio.LOOPS, 60, "for(int i=0; i<5; i++) { }"));

        // Desafíos de Funciones
        desafios.add(new Desafio("FUNC-001", "Diseñador de Métodos",
                "Crear un método que reciba dos enteros y retorne su suma",
                TipoDesafio.FUNCIONES, 70, "public int sumar(int a, int b) {"));

        // Desafíos de OOP
        desafios.add(new Desafio("OOP-001", "Constructor y Getter",
                "Crear una clase Person con nombre y edad, incluir constructor y getter",
                TipoDesafio.OOP, 80, "public class Person { private String nombre; public Person(String n) { this.nombre = n; } public String getNombre() { return nombre; } }"));

        // Desafíos de Excepciones
        desafios.add(new Desafio("EXC-001", "Manejador de Excepciones",
                "Crear una try-catch que intente dividir un número entre cero y capturar la excepción",
                TipoDesafio.EXCEPCIONES, 75, "try { int x = 10 / 0; } catch (ArithmeticException e) { }"));

        System.out.println("ChallengeManager inicializado con " + desafios.size() + " desafíos.");
    }

    /**
     * Obtiene un desafío aleatorio del tipo especificado.
     *
     * @ tipo tipo de desafío
     * @return objeto Desafio o null si no hay disponibles
     */
    public Desafio obtenerDesafioAleatorio(TipoDesafio tipo) {
        List<Desafio> filtrados = new ArrayList<>();
        for (Desafio d : desafios) {
            if (d.getTipo() == tipo && !d.isCompletado()) {
                filtrados.add(d);
            }
        }

        if (filtrados.isEmpty()) {
            return null;
        }

        return filtrados.get(random.nextInt(filtrados.size()));
    }

    /**
     * Obtiene todos los desafíos pendientes (no completados) de un tipo específico.
     *
     * @ tipo tipo de desafío
     * @return lista de desafíos pendientes
     */
    public List<Desafio> obtenerDesafiosPendientes(TipoDesafio tipo) {
        List<Desafio> pendientes = new ArrayList<>();
        for (Desafio d : desafios) {
            if (d.getTipo() == tipo && !d.isCompletado()) {
                pendientes.add(d);
            }
        }
        return pendientes;
    }

    /**
     * Obtiene la lista de desafíos no completados (todos los tipos).
     *
     * @return lista de desafíos pendientes
     */
    public List<Desafio> getDesafiosNoCompletados() {
        return desafios.stream().filter(d -> !d.isCompletado()).collect(Collectors.toList());
    }

    /**
     * Evalúa si el jugador completó exitosamente un desafío.
     * En un entorno real, esto validaría el código del jugador.
     * Para fines del gamified arena, simulamos el éxito/falimento.
     *
     * @ desafio desafío a evaluar
     * @ éxito si el jugador lo completó
     * @return true si el desafío se consideró completado
     */
    public boolean evaluarDesafio(Desafio desafio, boolean exito) {
        if (desafio == null) return false;

        desafio.incrementarIntentos();
        if (exito) {
            desafio.setCompletado(true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Obtiene la tasa de éxito general como porcentaje.
     *
     * @return tasa de éxito como porcentaje (0-100)
     */
    public double obtenerTasaExito() {
        int total = desafios.size();
        int completados = (int) desafios.stream().filter(Desafio::isCompletado).count();
        if (total == 0) return 0.0;
        return (double) completados / total * 100;
    }

    // Getters estadísticos públicos
    public int getTotalDesafios() { return desafios.size(); }
    public int getDesafiosCompletadosTotales() { return (int) desafios.stream().filter(Desafio::isCompletado).count(); }
    public int getDesafiosPendientes() { return desafios.size() - getDesafiosCompletadosTotales(); }

    /**
     * Método principal para pruebas rápidas.
     */
    public static void main(String[] args) {
        ChallengeManager cm = new ChallengeManager();
        System.out.println("ChallengeManager tiene " + cm.getTotalDesafios() + " desafíos.");
    }
}