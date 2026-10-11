package com.simdev.java.basic.arena;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class GamifiedArenaTest {

    private GamifiedArena arena;
    private PlayerProfile jugador;

    @BeforeEach
    void setUp() {
        arena = new GamifiedArena("TestPlayer");
        jugador = arena.getJugador(); // Acceso reflexivo o método público
    }

    // Helper para acceder al jugador privado desde la prueba
    private PlayerProfile getJugador() {
        return arena instanceof GamifiedArena ? ((GamifiedArena) arena).getJugadorPrivate() : null;
    }

    // Método auxiliar para acceder al campo privado a través de reflexión
    private PlayerProfile getPrivateField(Object object, String fieldName) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (PlayerProfile) field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    @DisplayName("Test: Crear perfil de jugador por defecto")
    void testCrearPerfilPorDefecto() {
        PlayerProfile p = new PlayerProfile();
        assertNotNull(p);
        assertEquals("JugadorAnonimo", p.getNombre());
        assertEquals(1, p.getNivel());
        assertEquals(0, p.getExperiencia());
    }

    @Test
    @DisplayName("Test: Crear perfil con nombre personalizado")
    void testCrearPerfilConNombre() {
        PlayerProfile p = new PlayerProfile("DesarrolladorJava");
        assertNotNull(p);
        assertEquals("DesarrolladorJava", p.getNombre());
        assertEquals(1, p.getNivel());
    }

    @Test
    @DisplayName("Test: Sumar experiencia y subida de nivel")
    void testSumarExperienciaYSubidaDeNivel() {
        PlayerProfile p = new PlayerProfile("TestUser");

        // Añadir 150 XP (debería subir de nivel una vez: 100 para nivel 1->2, dejando 50 de extra)
        boolean subio = p.sumarExperiencia(150);
        assertTrue(subio, "Debería subir de nivel con 150 XP");
        assertEquals(2, p.getNivel(), "Debería estar en el nivel 2");
        assertEquals(50, p.getExperiencia(), "Debería tener 50 XP restante después de subir al nivel 2");

        // Añadir 200 XP más (debería subir del nivel 2 al 3)
        subio = p.sumarExperiencia(200);
        assertTrue(subio, "Debería subir de nivel con 200 XP adicionales");
        assertEquals(3, p.getNivel(), "Debería estar en el nivel 3");

        // Calcular XP necesaria para siguiente nivel
        int xpSiguienteNivel = 100 + (3 - 1) * 50; // = 200
        assertEquals(0, p.getExperiencia(), "Debería tener 0 XP exactamente al subir al nivel 3");
    }

    @Test
    @DisplayName("Test: Precisión después de completados y fallidos")
    void testPrecisionDesafios() {
        PlayerProfile p = new PlayerProfile("PrecisionTest");

        // Completar 8 desafíos
        for (int i = 0; i < 8; i++) {
            p.incrementarDesafiosCompletados();
        }

        // Fallar 2 desafíos
        for (int i = 0; i < 2; i++) {
            p.incrementarDesafiosFallidos();
        }

        assertEquals(8, p.getDesafiosCompletados(), "Debería tener 8 completados");
        assertEquals(2, p.getDesafiosFallidos(), "Debería tener 2 fallidos");
        assertEquals(80.0, p.getPrecision(), "Debería tener 80.0% de precisión");
    }

    @Test
    @DisplayName("Test: Desbloqueo de logros")
    void testDesbloquearLogros() {
        PlayerProfile p = new PlayerProfile("LogrosTest");

        // Desbloquear varios logros
        p.desbloquearLogro("Primeros Pasos");
        p.desbloquearLogro("Nivel 5");
        p.desbloquearLogro("Experto Java");

        assertEquals(3, p.getCantidadLogros(), "Debería tener 3 logros");
        assertTrue(p.tieneLogro("Primeros Pasos"), "Debería tener el logro 'Primeros Pasos'");
        assertTrue(p.tieneLogro("Nivel 5"), "Debería tener el logro 'Nivel 5'");
        assertTrue(p.tieneLogro("Experto Java"), "Debería tener el logro 'Experto Java'");
        assertFalse(p.tieneLogro("Logro Inexistente"), "No debería tener un logro inexistente");
    }

    @Test
    @DisplayName("Test: Formato de nivel con barra de progreso")
    void testFormatoNivel() {
        PlayerProfile p = new PlayerProfile("FormatoTest");

        // Dar suficiente XP para estar al 75% del nivel 1
        p.sumarExperiencia(75); // 75/100 = 75%

        String formato = p.obtenerFormatoNivel();
        assertNotNull(formato, "El formato no debe ser nulo");
        assertTrue(formato.contains("Nivel 1"), "Debe mostrar Nivel 1");
        assertTrue(formato.contains("75"), "Debe mostrar 75% de progreso");
        assertTrue(formato.contains("▮"), "Debe contener barras de progreso");
    }

    @Test
    @DisplayName("Test: ChallengeManager - Inicialización de desafíos")
    void testChallengeManagerInicializacion() {
        ChallengeManager cm = new ChallengeManager();

        assertNotNull(cm);
        assertTrue(cm.getTotalDesafios() > 0, "Debería tener desafíos inicializados");
        assertTrue(cm.getTotalDesafios() >= 7, "Debería tener al menos 7 desafíos (uno por tipo)");

        // Verificar tipos de desafío
        boolean tieneOperadores = false;
        boolean tieneOOP = false;
        for (ChallengeManager.Desafio d : cm.desafios) {
            if (d.getTipo() == ChallengeManager.TipoDesafio.OPERADORES) tieneOperadores = true;
            if (d.getTipo() == ChallengeManager.TipoDesafio.OOP) tieneOOP = true;
        }
        assertTrue(tieneOperadores, "Debería tener al menos un desafío de operadores");
        assertTrue(tieneOOP, "Debería tener al menos un desafío de OOP");
    }

    @Test
    @DisplayName("Test: ChallengeManager - Evaluar desafío completado")
    void testEvaluarDesafioCompletado() {
        ChallengeManager cm = new ChallengeManager();

        // Obtener un desafío aleatorio
        ChallengeManager.Desafio d = cm.obtenerDesafioAleatorio(ChallengeManager.TipoDesafio.OPERADORES);
        assertNotNull(d, "Debería obtener un desafío");

        // Evaluar como completado
        boolean resultado = cm.evaluarDesafio(d, true);
        assertTrue(resultado, "Debería retornar true para éxito");
        assertTrue(d.isCompletado(), "El desafío debería marcarse como completado");
        assertEquals(1, cm.getDesafiosCompletadosTotales(), "Debería haber 1 desafío completado en total");
    }

    @Test
    @DisplayName("Test: ChallengeManager - Evaluar desafío fallido")
    void testEvaluarDesafioFallido() {
        ChallengeManager cm = new ChallengeManager();

        ChallengeManager.Desafio d = cm.obtenerDesafioAleatorio(ChallengeManager.TipoDesafio.OPERADORES);
        assertNotNull(d);

        // Evaluar como fallido
        boolean resultado = cm.evaluarDesafio(d, false);
        assertFalse(resultado, "Debería retornar false para fallo");
        assertFalse(d.isCompletado(), "El desafío no debería marcarse como completado");
        assertEquals(1, cm.getDesafiosCompletadosTotales(), "Aún no debería haber desafíos completados en total");
    }

    @Test
    @DisplayName("Test: ChallengeManager - Estadísticas de éxito")
    void testEstadisticasExito() {
        ChallengeManager cm = new ChallengeManager();

        // Simular varios desafíos completados y fallidos
        for (int i = 0; i < 5; i++) {
            ChallengeManager.Desafio d = cm.obtenerDesafioAleatorio(ChallengeManager.TipoDesafio.OPERADORES);
            if (d != null) {
                cm.evaluarDesafio(d, true); // 5 completados
            }
        }
        for (int i = 0; i < 2; i++) {
            ChallengeManager.Desafio d = cm.obtenerDesafioAleatorio(ChallengeManager.TipoDesafio.OPERADORES);
            if (d != null) {
                cm.evaluarDesafio(d, false); // 2 fallidos
            }
        }

        String reporte = cm.obtenerReporteEstadisticas();
        assertNotNull(reporte, "El reporte no debe ser nulo");
        assertTrue(reporte.contains("5"), "Debería mencionar 5 desafíos completados");
        assertTrue(reporte.contains("2"), "Debería mencionar 2 desafíos fallidos");
        assertTrue(reporte.contains("%"), "Debería contener porcentaje de tasa de éxito");
    }
}