package com.simdev.java.basic.arena;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/* Desafio y TipoDesafio son clases anidadas estáticas públicas de ChallengeManager */
import static com.simdev.java.basic.arena.ChallengeManager.Desafio;
import static com.simdev.java.basic.arena.ChallengeManager.TipoDesafio;

/**
 * Motor principal de la Gamified Arena Java.
 * Proporciona un sistema interactivo de progreso donde el usuario asume
 * el rol de programador en una arena, gana experiencia (XP), supera
 * desafíos asociados a módulos Java y desbloquea niveles e insignias.
 */
public class GamifiedArena {

    private PlayerProfile jugador;
    private ChallengeManager gestorDesafios;
    private Scanner scanner;

    // Mensajes de bienvenida y menú
    private static final String MENSAJE_BIENVENIDA =
            "==========================================" +
                    "\n     JAVA CLI GAMIFIED ARENA" +
                    "\n==========================================" +
                    "\n¡Bienvenido, programador! Tu viaje comienza aquí." +
                    "\nGana XP, sube de nivel y domina Java." +
                    "\n==========================================";

    private static final String[] OPCIONES_MENU = {
            "1. Iniciar Nuevo Perfil",
            "2. Ver Perfil Actual",
            "3. Desafío Aleatorio",
            "4. Ver Mis Desafíos",
            "5. Responder Desafío",
            "6. Ver Estadísticas",
            "7. Exportar Insignia (Markdown)",
            "8. Salir del Juego"
    };

    /**
     * Constructor que inicializa una nueva arena con un perfil de jugador.
     *
     * @ nombre nombre del jugador
     */
    public GamifiedArena(String nombre) {
        this.jugador = new PlayerProfile(nombre);
        this.gestorDesafios = new ChallengeManager();
        this.scanner = new Scanner(System.in);
    }

    // Constructor por defecto.
    public GamifiedArena() {
        this("ProgramadorAnonimo");
    }

    /**
     * Inicia la arena y muestra el bucle de menú principal.
     */
    public void iniciar() {
        System.out.println(MENSAJE_BIENVENIDA);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean activo = true;
        while (activo) {
            mostrarMenu();
            int opcion = obtenerOpcionValida(1, 8);

            switch (opcion) {
                case 1:
                    iniciarNuevoPerfil();
                    break;
                case 2:
                    verPerfil();
                    break;
                case 3:
                    desafioAleatorio();
                    break;
                case 4:
                    verDesafios();
                    break;
                case 5:
                    responderDesafio();
                    break;
                case 6:
                    verEstadisticas();
                    break;
                case 7:
                    exportarInsignia();
                    break;
                case 8:
                    activo = salirJuego();
                    break;
            }
        }
        System.out.println("\n¡Gracias por jugar! Hasta la próxima, " + jugador.getNombre() + ".");
        scanner.close();
    }

    /**
     * Muestra el menú de opciones en consola.
     */
    private void mostrarMenu() {
        System.out.println("\n=== MENU DE LA ARENA ===");
        for (String opcion : OPCIONES_MENU) {
            System.out.println(opcion);
        }
        System.out.print("\nSeleccione una opción: ");
    }

    /**
     * Obtiene una opción válida del menú con validación básica.
     *
     * @ min minimo valor permitido
     * @ max maximo valor permitido
     * @return opción válida seleccionada
     */
    private int obtenerOpcionValida(int min, int max) {
        int opcion = 0;
        boolean valida = false;
        while (!valida) {
            try {
                String input = scanner.nextLine();
                opcion = Integer.parseInt(input);
                if (opcion >= min && opcion <= max) {
                    valida = true;
                } else {
                    System.out.print("Opción fuera de rango. Seleccione entre " + min + " y " + max + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Por favor, ingrese un número: ");
            }
        }
        return opcion;
    }

    /**
     * Inicia un nuevo perfil de jugador (puede sobrescribir el actual).
     */
    private void iniciarNuevoPerfil() {
        System.out.print("\nIngrese su nombre de jugador: ");
        String nombre = scanner.nextLine();
        jugador = new PlayerProfile(nombre);
        System.out.println("\n¡Perfil creado exitosamente! Bienvenido, " + nombre + ".");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Muestra el perfil actual del jugador con todos sus estadísticos.
     */
    private void verPerfil() {
        if (jugador == null) {
            System.out.println("\nNo hay perfil activo. Primero cree un perfil (opción 1).");
            return;
        }

        System.out.println("\n=== PERFIL DEL JUGADOR ===");
        System.out.println(jugador.toString());
        System.out.println("\nFormato de Nivel: " + jugador.obtenerFormatoNivel());
        System.out.println("\nLogros Desbloqueados:");
        if (jugador.getCantidadLogros() == 0) {
            System.out.println("  (Ningún logro desbloqueado aún)");
        } else {
            for (String logro : jugador.getLogrosDesbloqueados()) {
                System.out.println("  - " + logro);
            }
        }
    }

    /**
     * Obtiene y presenta un desafío aleatorio al jugador.
     */
    private void desafioAleatorio() {
        if (jugador == null) {
            System.out.println("\nNo hay perfil activo. Primero cree un perfil.");
            return;
        }

        // Elegir un tipo de desafío aleatorio
        TipoDesafio[] tipos = TipoDesafio.values();
        int indice = new Random().nextInt(tipos.length);
        TipoDesafio tipoSeleccionado = tipos[indice];

        System.out.println("\n=== DESAFÍO ALEATORIO ===");
        System.out.println("Tipo: " + tipoSeleccionado);

        Desafio desafio = gestorDesafios.obtenerDesafioAleatorio(tipoSeleccionado);
        if (desafio == null) {
            System.out.println("No hay más desafíos pending de este tipo. Intente otro tipo.");
            return;
        }

        System.out.println("\nDesafío: " + desafio.getTitulo());
        System.out.println("Descripción: " + desafio.getDescripcion());
        System.out.println("Código Referencia: " + desafio.getCodigoReferencia());
        System.out.println("\n¡Intente resolverlo! (presione Enter para marcar como completado o 's' para saltar)");

        String respuesta = scanner.nextLine();
        if (respuesta.equalsIgnoreCase("s")) {
            System.out.println("Desafío saltado.");
            gestorDesafios.evaluarDesafio(desafio, false);
        } else {
            // En un entorno real, aquí validaríamos el código del usuario
            // Para el gamified arena, simulamos éxito aleatorio
            boolean exito = Math.random() > 0.3; // 70% de probabilidad de éxito
            boolean completado = gestorDesafios.evaluarDesafio(desafio, exito);

            if (completado) {
                jugador.sumarExperiencia(desafio.getPuntosXP());
                System.out.println("\n🎉 ¡Desafío Completo! Ganaste " + desafio.getPuntosXP() + " XP.");
                System.out.println("Nuevo nivel: " + jugador.getNivel());
            } else {
                System.out.println("\n❌ Desafío Fallido.");
                System.out.println("Ganaste 0 XP. Intenta otro desafío.");
            }
        }
    }

    /**
     * Muestra todos los desafíos disponibles (completados y pendientes).
     */
    private void verDesafios() {
        if (jugador == null) {
            System.out.println("\nNo hay perfil activo.");
            return;
        }

        System.out.println("\n=== MIS DESAFÍOS ===");
        System.out.println(gestorDesafios.obtenerTasaExito() + "% tasa de éxito general");

        System.out.println("\nDesafíos Pendientes por Tipo:");
        for (TipoDesafio tipo : TipoDesafio.values()) {
            List<Desafio> pendientes = gestorDesafios.obtenerDesafiosPendientes(tipo);
            if (!pendientes.isEmpty()) {
                System.out.println("\n" + tipo + " (" + pendientes.size() + " pendientes):");
                // Mostrar max 3
                int maxMostrar = Math.min(3, pendientes.size());
                for (int i = 0; i < maxMostrar; i++) {
                    System.out.println("  - " + pendientes.get(i).getTitulo() + " (" + pendientes.get(i).getPuntosXP() + " XP)");
                }
                if (pendientes.size() > 3) {
                    System.out.println("    ... y " + (pendientes.size() - 3) + " más");
                }
            }
        }
    }

    /**
     * Simula la respuesta a un desafío (en un entorno real, aquí entraría validación de código).
     */
    private void responderDesafio() {
        if (jugador == null) {
            System.out.println("\nNo hay perfil activo.");
            return;
        }

        System.out.println("\n=== RESPONDER DESAFÍO ===");
        System.out.println("Este módulo simula la validación de respuestas.");
        System.out.println("En un entorno IDE real, aquí se validaría el código del usuario.");
        System.out.println("Para este gamified arena, seleccionaremos un desafío aleatorio.");

        // Seleccionar un desafío pendiente
        List<Desafio> pendientes = new ArrayList<>();
        List<ChallengeManager.Desafio> allPendientes = gestorDesafios.getDesafiosNoCompletados();
        for (ChallengeManager.Desafio d : allPendientes) {
            pendientes.add(d);
        }

        if (pendientes.isEmpty()) {
            System.out.println("\n¡Felicidades! No te quedan desafíos pendientes.");
            System.out.println("Todos tus desafíos están completos. ¡Excelente progreso!");
            return;
        }

        // Seleccionar uno aleatorio
        Desafio seleccionado = pendientes.get(new Random().nextInt(pendientes.size()));
        System.out.println("\nDesafío: " + seleccionado.getTitulo());
        System.out.println("Tipo: " + seleccionado.getTipo());
        System.out.println("Descripción: " + seleccionado.getDescripcion());

        System.out.print("\n¿Fue exitoso tu intento? (s/n): ");
        String respuesta = scanner.nextLine();
        boolean exito = respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("y");

        boolean completado = gestorDesafios.evaluarDesafio(seleccionado, exito);

        if (completado) {
            jugador.sumarExperiencia(seleccionado.getPuntosXP());
            System.out.println("\n🎉 ¡Desafío Completo!");
            System.out.println("Ganaste " + seleccionado.getPuntosXP() + " XP.");
            System.out.println("Nivel actual: " + jugador.getNivel());
            System.out.println("Precisión actual: " + String.format("%.1f", jugador.getPrecision()) + "%");
        } else {
            System.out.println("\n❌ Desafío Fallido.");
            System.out.println("No ganaste XP esta vez. Intenta otro desafío.");
        }
    }

    /**
     * Muestra estadísticas resumidas del jugador.
     */
    private void verEstadisticas() {
        if (jugador == null) {
            System.out.println("\nNo hay perfil activo.");
            return;
        }

        System.out.println("\n=== ESTADÍSTICAS DEL JUGADOR ===");
        System.out.println(jugador.obtenerFormatoNivel());
        System.out.println("\nResumen Rápido:");
        System.out.println("  • XP Actual: " + jugador.getExperiencia() + "/" + jugador.getExperienciaParaNivel());
        System.out.println("  • Nivel: " + jugador.getNivel());
        System.out.println("  • Precisión: " + String.format("%.1f", jugador.getPrecision()) + "%");
        System.out.println("  • Desafíos Completados: " + jugador.getDesafiosCompletados());
        System.out.println("  • Desafíos Fallidos: " + jugador.getDesafiosFallidos());
        System.out.println("  • Tasa de Éxito: " + String.format("%.1f", jugador.getPrecision()) + "%");

        System.out.println("\nDesafíos Totales en Arena: " + gestorDesafios.getTotalDesafios());
        System.out.println("Desafíos Completados en Arena: " + gestorDesafios.getDesafiosCompletadosTotales());
        System.out.println("Desafíos Pendientes en Arena: " + gestorDesafios.getDesafiosPendientes());
    }

    /**
     * Exporta el perfil del jugador como un archivo Markdown para GitHub.
     */
    private void exportarInsignia() {
        if (jugador == null) {
            System.out.println("\nNo hay perfil activo para exportar.");
            return;
        }

        // Generar contenido Markdown
        StringBuilder md = new StringBuilder();
        md.append("# ").append(jugador.getNombre()).append(" - Insignia de Gamified Arena\n\n");
        md.append("## Perfil del Programador\n\n");
        md.append("- **Nivel:** ").append(jugador.getNivel()).append("\n");
        md.append("- **Experiencia:** ").append(jugador.getExperiencia()).append(" / ").append(jugador.getExperienciaParaNivel()).append("\n");
        md.append("- **Precisión:** ").append(String.format("%.1f", jugador.getPrecision())).append("%\n");
        md.append("- **Desafíos Completados:** ").append(jugador.getDesafiosCompletados()).append("\n");
        md.append("- **Desafíos Fallidos:** ").append(jugador.getDesafiosFallidos()).append("\n");
        md.append("- **Tasa de Éxito:** ").append(String.format("%.1f", jugador.getPrecision())).append("%\n");
        md.append("- **Logros:** ").append(jugador.getCantidadLogros()).append(" desbloqueados\n\n");
        md.append("## Logros Desbloqueados\n\n");
        if (jugador.getCantidadLogros() == 0) {
            md.append("*Ningún logro desbloqueado aún*\n\n");
        } else {
            for (String logro : jugador.getLogrosDesbloqueados()) {
                md.append("- " + logro + "\n");
            }
        }
        md.append("\n## Desafíos en la Gamified Arena\n\n");
        md.append("- **Total de Desafíos:** " + gestorDesafios.getTotalDesafios() + "\n");
        md.append("- **Completados:** " + gestorDesafios.getDesafiosCompletadosTotales() + "\n");
        md.append("- **Pendientes:** " + gestorDesafios.getDesafiosPendientes() + "\n");
        md.append("- **Tasa de Éxito:** " + String.format("%.1f", gestorDesafios.obtenerTasaExito()) + "%\n\n");
        md.append("---\n*Generado con JavaCLI Gamified Arena*");

        // Mostrar en consola y ofrecer exportar a archivo
        System.out.println("\n=== INSIGNA MARKDOWN GENERADA ===\n");
        System.out.println(md.toString());

        System.out.print("\n¿Desea guardar este markdown en un archivo 'profile-badge.md'? (s/n): ");
        String guardar = scanner.nextLine();
        if (guardar.equalsIgnoreCase("s")) {
            try {
                java.io.File file = new java.io.File("profile-badge.md");
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(md.toString());
                writer.close();
                System.out.println("\n✅ Insignia guardada exitosamente en 'profile-badge.md'");
                System.out.println("   Puedes subir este archivo a tu perfil de GitHub.");
            } catch (IOException e) {
                System.err.println("\n❌ Error al guardar el archivo: " + e.getMessage());
            }
        } else {
            System.out.println("\nInsignia no guardada.");
        }
    }

    /**
     * Pregunta si desea salir del juego.
     *
     * @return true si se debe salir, false para continuar
     */
    private boolean salirJuego() {
        System.out.print("\n¿Estás seguro de que quieres salir de la arena? (s/n): ");
        String respuesta = scanner.nextLine();
        return respuesta.equalsIgnoreCase("s");
    }

    // Helper method para generar número aleatorio dentro de un rango
    private int randomInt(int max) {
        return new Random().nextInt(max);
    }

    /**
     * Punto de entrada principal para la Gamified Arena.
     *
     * @ args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("Iniciando JavaCLI Gamified Arena...");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GamifiedArena arena = new GamifiedArena();
        arena.iniciar();
    }
}