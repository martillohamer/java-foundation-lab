package com.simdev.java.basic.visualizer;

import java.lang.reflect.*;
import java.util.*;

/**
 * Visualizador de bytecode y metadatos de clases Java.
 * Utiliza la reflexión avanzada de Java para inspeccionar la estructura
 * interna de clases compiladas y presentar los metadatos de manera
 * organizada en la consola.
 */
public class BytecodeVisualizer {

    /**
     * Inspecciona una clase y muestra su metadatos estructurados en consola.
     *
     * @ nombreClase Nombre completo de la clase a inspeccionar (ej. "com.simdev.java.basic.c00_helloworld.HelloWorld")
     */
    public void inspectarClase(String nombreClase) {
        try {
            // Cargar la clase usando el classloader del sistema
            Class<?> clazz = Class.forName(nombreClase);

            // Información básica
            StringBuilder sb = new StringBuilder();
            sb.append("==========================================");
            sb.append("\n");
            sb.append("      VISBYTE - Visualizador de Bytecode JVM");
            sb.append("\n");
            sb.append("==========================================");
            sb.append("\n\n");

            // Nombre de la clase
            sb.append("CLASS: ").append(clazz.getName()).append("\n");
            sb.append("-------------------------------");
            sb.append("\n");

            // Modificadores
            sb.append("MODIFIERS: ");
            String modifiers = obtenerModificadores(clazz.getModifiers());
            sb.append(modifiers).append("\n\n");

            // Superclase
            sb.append("SUPERCLASS: ");
            if (clazz.getSuperclass() != null) {
                sb.append(clazz.getSuperclass().getName());
            } else {
                sb.append("java.lang.Object (raíz)");
            }
            sb.append("\n\n");

            // Interfaces
            sb.append("INTERFACES: ");
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                for (Class<?> face : interfaces) {
                    sb.append(face.getName()).append(" ");
                }
            } else {
                sb.append("(ninguna)");
            }
            sb.append("\n\n");

            // Campos
            sb.append("FIELDS: ");
            Field[] fields = clazz.getDeclaredFields();
            if (fields.length > 0) {
                for (Field field : fields) {
                    sb.append("- ").append(modificadoresCampo(field.getModifiers())).append(" ")
                            .append(field.getType().getSimpleName()).append(" ")
                            .append(field.getName()).append("\n");
                }
            } else {
                sb.append("(ninguno)");
            }
            sb.append("\n");

            // Constructores
            sb.append("CONSTRUCTORS: ");
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            if (constructors.length > 0) {
                for (Constructor<?> constructor : constructors) {
                    sb.append("- ").append(modificadoresMetodo(constructor.getModifiers())).append(" ")
                            .append(constructor.getDeclaringClass().getSimpleName()).append(".")
                            .append(constructor.getName()).append("(");
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    for (int i = 0; i < paramTypes.length; i++) {
                        sb.append(paramTypes[i].getSimpleName());
                        if (i < paramTypes.length - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append(")\n");
                }
            } else {
                sb.append("(ninguno)");
            }
            sb.append("\n");

            // Métodos
            sb.append("METHODS: ");
            Method[] methods = clazz.getMethods();
            if (methods.length > 0) {
                for (Method method : methods) {
                    sb.append("- ").append(modificadoresMetodo(method.getModifiers())).append(" ")
                            .append(method.getReturnType().getSimpleName()).append(" ")
                            .append(method.getName()).append("(");
                    Class<?>[] paramTypes = method.getParameterTypes();
                    for (int i = 0; i < paramTypes.length; i++) {
                        sb.append(paramTypes[i].getSimpleName());
                        if (i < paramTypes.length - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append(")").append("\n");
                }
            } else {
                sb.append("(ninguno)");
            }
            sb.append("\n");

            sb.append("==========================================");
            sb.append("\n");
            System.out.print(sb.toString());

        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontró la clase: " + nombreClase);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR al inspeccionar la clase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene una representación en String de los modificadores de una clase.
     *
     * @param modifiers Flags de modificadores int
     * @return Cadena de modificadores (public, private, protected, static, etc.)
     */
    private String obtenerModificadores(int modifiers) {
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

    /**
     * Obtiene una representación en String de los modificadores de un campo.
     *
     * @param modifiers Flags de modificadores int
     * @return Cadena de modificadores de campo
     */
    private String modificadoresCampo(int modifiers) {
        StringBuilder mod = new StringBuilder();
        if (java.lang.reflect.Modifier.isPublic(modifiers)) mod.append("public ");
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) mod.append("private ");
        if (java.lang.reflect.Modifier.isProtected(modifiers)) mod.append("protected ");
        if (java.lang.reflect.Modifier.isStatic(modifiers)) mod.append("static ");
        if (java.lang.reflect.Modifier.isFinal(modifiers)) mod.append("final ");
        return mod.toString().trim();
    }

    /**
     * Obtiene una representación en String de los modificadores de un método o constructor.
     *
     * @param modifiers Flags de modificadores int
     * @return Cadena de modificadores de método
     */
    private String modificadoresMetodo(int modifiers) {
        StringBuilder mod = new StringBuilder();
        if (java.lang.reflect.Modifier.isPublic(modifiers)) mod.append("public ");
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) mod.append("private ");
        if (java.lang.reflect.Modifier.isProtected(modifiers)) mod.append("protected ");
        if (java.lang.reflect.Modifier.isStatic(modifiers)) mod.append("static ");
        if (java.lang.reflect.Modifier.isFinal(modifiers)) mod.append("final ");
        if (java.lang.reflect.Modifier.isSynchronized(modifiers)) mod.append("synchronized ");
        if (java.lang.reflect.Modifier.isNative(modifiers)) mod.append("native ");
        if (mod.isEmpty()) return "default";
        return mod.toString().trim();
    }

    /**
     * Inspecciona todas las clases de un paquete (búsqueda recursiva limitada).
     *
     * @ paquete Paquete a inspeccionar (ej. "com.simdev.java.basic.c00_helloworld")
     */
    public void inspeccionarPaquete(String paquete) {
        System.out.println("Inspeccionando paquete: " + paquete);
        System.out.println("Esta funcionalidad requiere acceso al filesystem de clases.");
        System.out.println("Use inspectarClase() para inspeccionar clases específicas.\n");
    }

    /**
     * Punto de entrada principal para pruebas rápidas.
     *
     * @ args Argumentos de línea de comandos: [nombre_clase]
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java BytecodeVisualizer <nombre_clase>");
            System.out.println("Ejemplo: java BytecodeVisualizer com.simdev.java.basic.c00_helloworld.HelloWorld");
            return;
        }

        BytecodeVisualizer visualizer = new BytecodeVisualizer();
        visualizer.inspectarClase(args[0]);
    }
}