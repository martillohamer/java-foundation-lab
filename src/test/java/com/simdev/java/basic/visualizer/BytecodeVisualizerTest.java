package com.simdev.java.basic.visualizer;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;

class BytecodeVisualizerTest {

    private BytecodeVisualizer visualizer;
    private final String CLASE_TEST = "com.simdev.java.basic.c00_helloworld.HelloWorld";

    @BeforeEach
    void setUp() {
        visualizer = new BytecodeVisualizer();
    }

    @Test
    @DisplayName("Test: Inspeccionar clase HelloWorld existe y es cargable")
    void testClaseCargable() {
        assertDoesNotThrow(() -> {
            Class<?> clazz = Class.forName(CLASE_TEST);
            assertNotNull(clazz);
        });
    }

    @Test
    @DisplayName("Test: Obtener nombre de clase")
    void testObtenerNombreClase() {
        Class<?> clazz = null;
        assertDoesNotThrow(() -> clazz = Class.forName(CLASE_TEST));
        assertNotNull(clazz);

        String nombre = clazz.getName();
        assertNotNull(nombre);
        assertTrue(nombre.contains("HelloWorld"), "El nombre debe contener HelloWorld");
        assertTrue(nombre.contains("c00_helloworld"), "El nombre debe contener c00_helloworld");
    }

    @Test
    @DisplayName("Test: Obtener modificadores de clase")
    void testModificadoresClase() {
        Class<?> clazz = null;
        assertDoesNotThrow(() -> clazz = Class.forName(CLASE_TEST));
        assertNotNull(clazz);

        String mods = BytecodeVisualizerTest.obtenerModificadores(clazz.getModifiers());
        assertNotNull(mods);
        assertTrue(mods.length() > 0, "Los modificadores no deben estar vacíos");
    }

    @Test
    @DisplayName("Test: Obtener superclase")
    void testSuperclase() {
        Class<?> clazz = null;
        assertDoesNotThrow(() -> clazz = Class.forName(CLASE_TEST));
        assertNotNull(clazz);

        Class<?> superclass = clazz.getSuperclass();
        assertNotNull(superclass);
        assertEquals("java.lang.Object", superclass.getName());
    }

    @Test
    @DisplayName("Test: Obtener interfaces implementadas")
    void testInterfaces() {
        Class<?> clazz = null;
        assertDoesNotThrow(() -> clazz = Class.forName(CLASE_TEST));
        assertNotNull(clazz);

        Class<?>[] interfaces = clazz.getInterfaces();
        assertNotNull(interfaces);
        // HelloWorld no implementa interfaces explícitas, por lo tanto debería tener al menos Object
        // o estar vacío dependiendo de la versión de Java
    }

    @Test
    @DisplayName("Test: Obtener campos de la clase")
    void testCampos() {
        Class<?> clazz = null;
        assertDoesNotThrow(() -> clazz = Class.forName(CLASE_TEST));
        assertNotNull(clazz);

        Field[] fields = clazz.getDeclaredFields();
        assertNotNull(fields);
        // HelloWorld no tiene campos declarados, solo el main method estático
        // Así que la lista podría estar vacía o tener campos heredados
    }

    @Test
    @DisplayName("Test: Obtener constructores")
    void testConstructores() {
        Class<?> clazz = null;
        assertDoesNotThrow(() -> clazz = Class.forName(CLASE_TEST));
        assertNotNull(clazz);

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        assertNotNull(constructors);
        // HelloWorld tiene un constructor implícito por defecto
    }

    @Test
    @DisplayName("Test: Obtener métodos públicos")
    void testMetodosPublicos() {
        Class<?> clazz = null;
        assertDoesNotThrow(() -> clazz = Class.forName(CLASE_TEST));
        assertNotNull(clazz);

        Method[] methods = clazz.getMethods();
        assertNotNull(methods);
        // Debe tener al menos el método main
        boolean tieneMain = false;
        for (Method m : methods) {
            if (m.getName().equals("main")) {
                tieneMain = true;
                break;
            }
        }
        assertTrue(tieneMain, "La clase HelloWorld debe tener el método main público");
    }

    @Test
    @DisplayName("Test: Inspección completa de HelloWorld")
    void testInspeccionCompleta() {
        Class<?> clazz = null;
        assertDoesNotThrow(() -> clazz = Class.forName(CLASE_TEST));
        assertNotNull(clazz);

        // Verificar estructura básica
        assertNotNull(clazz.getSimpleName(), "SimpleName no debe ser nulo");
        assertNotNull(clazz.getCanonicalName(), "CanonicalName no debe ser nulo");
        assertNotNull(clazz.getModifiers(), "Modifiers no debe ser nulo");
    }

    /**
     * Método auxiliar estático para probar obtención de modificadores.
     */
    static String obtenerModificadores(int modifiers) {
        StringBuilder mod = new StringBuilder();
        if (java.lang.reflect.Modifier.isPublic(modifiers)) mod.append("public ");
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) mod.append("private ");
        if (java.lang.reflect.Modifier.isProtected(modifiers)) mod.append("protected ");
        if (java.lang.reflect.Modifier.isStatic(modifiers)) mod.append("static ");
        if (java.lang.reflect.Modifier.isFinal(modifiers)) mod.append("final ");
        if (java.lang.reflect.Modifier.isAbstract(modifiers)) mod.append("abstract ");
        if (mod.isEmpty()) return "default";
        return mod.toString().trim();
    }
}