package com.simdev.java.basic.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;

import org.objectweb.asm.*;
import org.objectweb.asm.util.ClassWriter;

/**
 * Transformador de bytecode que instrumenta métodos inyectando
 * medición de tiempo (System.nanoTime()) y conteo de llamadas.
 * Funciona en tiempo de carga de clases mediante el Instrumentation API.
 * <p>
 * Las transformaciones incluyen:
 * 1. Inyectar llamada a System.nanoTime() al entrada del método
 * 2. Inyectar llamada a System.nanoTime() al salida del método
 * 3. Incrementar contador de llamadas estático
 * 4. Capturar tiempo transcurrido y acumularlo
 * <p>
 * Esta implementación usa ObjectWeb ASM para el análisis y transformación
 * del bytecode Java.
 */
public class AgentTransformer implements ClassFileTransformer {

    // Patron de método para identificar métodos objetivo
    private static final String[] TARGET_METHOD_PATTERNS = {
            "execute", "run", "compute", "process", "handle",
            "transform", "analyze", "calculate", "validate"
    };

    // Nombre de la clase de estadísticas internas
    private static final String STATS_CLASS = "com/simdev/java/basic/agent/Agent$MethodStats";

    @Override
    public byte[] transform(Class<?> clazz, String productionName,
                            java.security.ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        // Solo transformar clases de nuestro paquete
        if (clazz == null || !clazz.getName().startsWith("com.simdev.java.basic")) {
            return null;
        }

        try {
            // Analizar el bytecode existente
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            // Visitar y transformar la clase
            classVisitor = new ClassWriterVisitor(classWriter, clazz.getName());
            classReader.accept(classVisitor, 0);

            // Devolver el bytecode transformado
            return classWriter.toByteArray();

        } catch (Exception e) {
            // En caso de error, devolver el buffer original
            e.printStackTrace();
            return classfileBuffer;
        }
    }

    // Clase interna para visitar la estructura de la clase
    private static class ClassWriterVisitor extends ClassVisitor {

        private final ClassWriter cw;
        private String className;

        public ClassWriterVisitor(ClassWriter cw, String className) {
            super(Opcodes.ASM9, cw);
            this.cw = cw;
            this.className = className;
        }

        @Override
        public int visitVersion(int version) {
            return super.visitVersion(version);
        }

        @Override
        public void visitSource(String source, String debug) {
            super.visitSource(source, debug);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            return super.visitField(access, name, descriptor, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {

            // Verificar si este método es un objetivo para instrumentación
            if (esMetodoObjetivo(access, name, descriptor)) {
                // Retornar un MethodVisitor que inyecta el código de medición
                return new MetodoInstrumentadorVisitor(access, name, descriptor, cw);
            }

            // Visitante estándar para métodos no instrumentados
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        private boolean esMetodoObjetivo(int access, String nombre, String descriptor) {
            // Solo instrumentar métodos públicos de instancia
            if ((access & Opcodes.ACC_PUBLIC) == 0) {
                return false;
            }
            if (nombre.equals("<init>") || nombre.equals("<clinit>")) {
                return false;
            }
            if (nombre.startsWith("lambda$")) {
                return false;
            }

            // Verificar nombre del método contra patrones objetivo
            for (String pattern : TARGET_METHOD_PATTERNS) {
                if (nombre.equals(pattern) || nombre.contains(pattern)) {
                    return true;
                }
            }

            // Instrumentar un subconjunto de métodos para minimizar overhead
            // Por ejemplo, cada 4to método que coincida con criterios básicos
            return "execute".equals(nombre) || "run".equals(nombre);
        }
    }

    /**
     * Visita del método que inyecta código de medición de tiempo.
     */
    private static class MetodoInstrumentadorVisitor extends MethodVisitor {

        private final String methodName;
        private final String descriptor;
        private final ClassWriter cw;

        public MetodoInstrumentadorVisitor(int access, String name, String descriptor, ClassWriter cw) {
            super(Opcodes.ASM9, cw);
            this.methodName = name;
            this.descriptor = descriptor;
            this.cw = cw;
        }

        @Override
        public void visitCode() {
            // Marcar el inicio del código del método
            super.visitCode();

            // Obtener el nombre del método para las etiquetas
            visitLabel(null); // Etiqueta inicial

            // Inyectar medición de tiempo de entrada
            insertarMedicionEntrada();
        }

        private void insertarMedicionEntrada() {
            // Cargar el objeto Agent.MethodStats de la clase outer
            // Cargar el nombre del método como string constante
            // Llamar a Agent.MethodStats.incrementCallCount()

            // System.nanoTime() entrada
            visitVarInsn(Opcodes.GETSTATIC,
                    "com/simdev/java/basic/agent/Agent",
                    "INITIAL_TIME_" + methodName.replaceAll("[^a-zA-Z0-9]", "_"),
                    "J");

            // Llamar a System.nanoTime()
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    "java/lang/System", "nanoTime", "()J", false);

            // Almacenar tiempo de inicio en variable local
            visitVarInsn(Opcodes.LSTORE, 0); // local0 = tiempoInicial

            // Incrementar contador de llamadas
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/simdev/java/basic/agent/Agent",
                    "incrementarLlamadaMetodo_" + methodName.replaceAll("[^a-zA-Z0-9]", "_"),
                    "()V", false);
        }

        @Override
        public void visitEnd() {
            // Inyectar medición de tiempo de salida justo antes del return
            // Calcular tiempo transcurrido = tiempoFinal - tiempoInicial
            // Sumar a totalNanos

            // Cargar tiempo final
            visitVarInsn(Opcodes.LLOAD, 0); // local0 ya tiene tiempoInicial, necesitamos tiempoFinal

            // Obtener nuevo tiempo System.nanoTime()
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    "java/lang/System", "nanoTime", "()J", false);

            // Restar tiempoInicial
            visitInsn(Opcodes.LSUB);

            // Agregar al total estadístico
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/simdev/java/basic/agent/Agent",
                    "acumularTiempoMetodo_" + methodName.replaceAll("[^a-zA-Z0-9]", "_"),
                    "(J)V", false);

            // Llamar al método original
            super.visitEnd();
        }
    }
}