package com.simdev.java.basic.arena;

/**
 * Representa el perfil de un jugador en la Gamified Arena.
 * Almacena estadísticas, progreso, logros y configuración del jugador.
 */
public class PlayerProfile {

    // Atributos del jugador
    private String nombre;
    private String rol;
    private int nivel;
    private int experiencia;
    private int experienciaParaNivel;
    private int desafiosCompletados;
    private int desafiosFallidos;
    private double precision;
    private String ultimaActividad;
    private String[] logrosDesbloqueados;

    // Constructor por defecto
    public PlayerProfile() {
        this.nombre = "JugadorAnonimo";
        this.rol = "Novato";
        this.nivel = 1;
        this.experiencia = 0;
        this.experienciaParaNivel = 100; // XP necesario para subir del nivel 1 al 2
        this.desafiosCompletados = 0;
        this.desafiosFallidos = 0;
        this.precision = 0.0;
        this.ultimaActividad = "Nunca";
        this.logrosDesbloqueados = new String[0];
    }

    // Constructor con nombre
    public PlayerProfile(String nombre) {
        this.nombre = nombre;
        this.rol = "Novato";
        this.nivel = 1;
        this.experiencia = 0;
        this.experienciaParaNivel = 100;
        this.desafiosCompletados = 0;
        this.desafiosFallidos = 0;
        this.precision = 0.0;
        this.ultimaActividad = "Nunca";
        this.logrosDesbloqueados = new String[0];
    }

    // Getters y Setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.ultimaActividad = "Cambio de nombre";
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public int getNivel() {
        return nivel;
    }

    /**
     * Establece el nivel y ajusta la experiencia accordingly.
     *
     * @param nuevoNivel nuevo nivel a establecer
     */
    public void setNivel(int nuevoNivel) {
        this.nivel = nuevoNivel;
        // Recalcular experiencia requerida para el nuevo nivel
        this.experienciaParaNivel = 100 + (nivel - 1) * 50;
    }

    public int getExperiencia() {
        return experiencia;
    }

    /**
     * Obtiene la experiencia requerida para el nivel actual.
     *
     * @return experiencia necesaria para subir de nivel
     */
    public int getExperienciaParaNivel() {
        return experienciaParaNivel;
    }

    /**
     * Añade experiencia al jugador y verifica subida de nivel.
     *
     * @param puntos XP a añadir
     * @return true si subió de nivel, false en caso contrario
     */
    public boolean sumarExperiencia(int puntos) {
        if (puntos <= 0) return false;
        this.experiencia += puntos;
        this.ultimaActividad = "Ganó " + puntos + " XP";

        // Verificar si sube de nivel
        while (this.experiencia >= this.experienciaParaNivel) {
            this.experiencia -= this.experienciaParaNivel;
            this.nivel++;
            this.experienciaParaNivel = 100 + (this.nivel - 1) * 50;
            // Desbloquear logro de nivel
            desbloquearLogro("Nivel " + this.nivel);
        }
        return this.nivel > 1; // Retorna true si hubo algún cambio de nivel
    }

    public int getDesafiosCompletados() {
        return desafiosCompletados;
    }

    public void incrementarDesafiosCompletados() {
        this.desafiosCompletados++;
        this.precision = (double) this.desafiosCompletados / (this.desafiosCompletados + this.desafiosFallidos) * 100;
        this.ultimaActividad = "Completó un desafío";
    }

    public int getDesafiosFallidos() {
        return desafiosFallidos;
    }

    public void incrementarDesafiosFallidos() {
        this.desafiosFallidos++;
        this.precision = (double) this.desafiosCompletados / (this.desafiosCompletados + this.desafiosFallidos) * 100;
        this.ultimaActividad = "Falló un desafío";
    }

    public double getPrecision() {
        return precision;
    }

    public String getUltimaActividad() {
        return ultimaActividad;
    }

    // Métodos de logros

    /**
     * Desbloquea un logro si no estaba ya desbloqueado.
     *
     * @ nombreLogro nombre del logro
     */
    public void desbloquearLogro(String nombreLogro) {
        // Verificar si ya existe
        boolean yaExiste = false;
        for (String logro : logrosDesbloqueados) {
            if (logro.equals(nombreLogro)) {
                yaExiste = true;
                break;
            }
        }

        if (!yaExiste) {
            // Crear nuevo array con uno más elemento
            String[] nuevosLogros = new String[logrosDesbloqueados.length + 1];
            for (int i = 0; i < logrosDesbloqueados.length; i++) {
                nuevosLogros[i] = logrosDesbloqueados[i];
            }
            nuevosLogros[logrosDesbloqueados.length] = nombreLogro;
            this.logrosDesbloqueados = nuevosLogros;
        }
    }

    public String[] getLogrosDesbloqueados() {
        return logrosDesbloqueados;
    }

    public int getCantidadLogros() {
        return logrosDesbloqueados.length;
    }

    /**
     * Verifica si el jugador tiene un logro específico.
     *
     * @ nombreLogro nombre del logro a verificar
     * @return true si tiene el logro, false en caso contrario
     */
    public boolean tieneLogro(String nombreLogro) {
        for (String logro : logrosDesbloqueados) {
            if (logro.equals(nombreLogro)) {
                return true;
            }
        }
        return false;
    }

    // Método para formatear el nivel con barra de progreso
    public String obtenerFormatoNivel() {
        double progreso = (double) experiencia / experienciaParaNivel * 100;
        int barrasCompletas = (int) (progreso / 10);
        int barrasVacias = 10 - barrasCompletas;

        StringBuilder sb = new StringBuilder();
        sb.append("Nivel ").append(nivel).append(" [");
        for (int i = 0; i < barrasCompletas; i++) sb.append("▮");
        for (int i = 0; i < barrasVacias; i++) sb.append("▯");
        sb.append("] ").append((int) progreso).append("%");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "PlayerProfile{" +
                "nombre='" + nombre + '\'' +
                ", rol='" + rol + '\'' +
                ", nivel=" + nivel +
                ", experiencia=" + experiencia + "/" + experienciaParaNivel +
                ", precision=" + String.format("%.1f", precision) + "%" +
                ", desafiosCompletados=" + desafiosCompletados +
                ", desafiosFallidos=" + desafiosFallidos +
                ", logros=" + java.util.Arrays.toString(logrosDesbloqueados) +
                '}';
    }
}