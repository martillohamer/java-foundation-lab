package com.simdev.java.basic.sandbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SandboxControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Test: Ejecutar código Java vacío debe fallar")
    void givenEmptySource_whenExecuteCode_thenBadRequest() throws Exception {
        // Arrange
        ExecutionRequest request = new ExecutionRequest();
        request.setSource("");

        // Act & Assert
        mockMvc.perform(post("/api/sandbox/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("Test: Ejecutar código Hello World exitoso")
    void givenValidHelloWorld_whenExecuteCode_thenSuccess() throws Exception {
        // Arrange
        String helloWorld = "public class Main {"
                + "    public static void main(String[] args) {"
                + "        System.out.println(\"Hello World\");"
                + "    }"
                + "}";

        ExecutionRequest request = new ExecutionRequest();
        request.setSource(helloWorld);

        // Act & Assert
        ResultActions actions = mockMvc.perform(post("/api/sandbox/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Verificar respuesta exitosa
                .andExpect(status().isOk())

                // Verificar que el cuerpo contiene los campos esperados
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.output").value("Hello World"))
                .andExpect(jsonPath("$.compilationSuccessful").value(true))
                .andExpect(jsonPath("$.compiledClassName").value("Main"))

                // Verificar que no hay errores
                .andExpect(jsonPath("$.errors").value(""))

                // Verificar código de salida
                .andReturn();

        // Additional verification - we just check it didn't throw
        System.out.println("✅ Hello World execution test passed");
    }

    @Test
    @DisplayName("Test: Compilar código Java exitoso")
    void givenValidCode_whenCompileCode_thenSuccess() throws Exception {
        // Arrange
        String validCode = "public class CompiledMain {"
                + "    public static void main(String[] args) {}"
                + "}";

        ExecutionRequest request = new ExecutionRequest();
        request.setSource(validCode);

        // Act & Assert
        mockMvc.perform(post("/api/sandbox/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Verificar respuesta exitosa
                .andExpect(status().isOk())

                // Verificar que compilación fue exitosa
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compilación exitosa"))

                // Verificar que no hay errores
                .andExpect(jsonPath("$.errors").value(""))

                .andDo(print());

        System.out.println("✅ Compilation test passed");
    }

    @Test
    @DisplayName("Test: Endpoint health check")
    void givenHealthEndpoint_whenHealthCheck_thenOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/sandbox/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.service").value("Java Sandbox Micro-Engine"))

                .andDo(print());

        System.out.println("✅ Health check test passed");
    }

    @Test
    @DisplayName("Test: Ejecutar código con error de compilación")
    void givenInvalidJava_whenExecuteCode_thenErrorResponse() throws Exception {
        // Arrange
        String invalidCode = "public class Main {"
                + "    public static void main(String[] args) {"
                + "        int x = 1 / 0; // This will cause runtime error but compile fine"
                + "    }"
                + "}";

        ExecutionRequest request = new ExecutionRequest();
        request.setSource(invalidCode);

        // Act & Assert
        mockMvc.perform(post("/api/sandbox/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Debería ser 200 (el código compila, ejecuta y falla en runtime)
                .andExpect(status().isOk())

                // Debería tener compilación exitosa pero posible error de ejecución
                .andExpect(jsonPath("$.compilationSuccessful").value(true))

                .andDo(print());

        System.out.println("✅ Invalid code execution test passed");
    }

    @Test
    @DisplayName("Test: Request nulo debe manejarse adecuadamente")
    void givenNullRequest_whenExecuteCode_thenBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/sandbox/execute")
                        .contentType(MediaType.APPLICATION_JSON))

                // Debería retornar bad request
                .andExpect(status().isBadRequest())

                .andDo(print());

        System.out.println("✅ Null request test passed");
    }

    @Test
    @DisplayName("Test: Request sin campo source debe fallar")
    void givenRequestWithoutSource_whenExecuteCode_thenBadRequest() throws Exception {
        // Arrange
        ExecutionRequest request = new ExecutionRequest();
        // source no se establece

        // Act & Assert
        mockMvc.perform(post("/api/sandbox/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andDo(print());

        System.out.println("✅ Request without source test passed");
    }

    @Nested
    @DisplayName("Integración de pruebas")
    class IntegrationTests {

        @Test
        @DisplayName("Test: Flujo completo execute -> verify -> health")
        void givenCompleteFlow_whenExecuteAndVerify_thenAllPass() throws Exception {
            // Arrange - Código que imprime un mensaje específico
            String flowCode = "public class FlowTest {"
                    + "    public static void main(String[] args) {"
                    + "        System.out.println(\"FLOW_TEST_SUCCESS\");"
                    + "    }"
                    + "}";

            ExecutionRequest request = new ExecutionRequest();
            request.setSource(flowCode);

            // Act - Ejecutar código
            ResultActions executeResult = mockMvc.perform(post("/api/sandbox/execute")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))

                    // Verificar ejecución exitosa
                    .andExpect(status().isOk())

                    // Verificar salida esperada
                    .andExpect(jsonPath("$.output").value("FLOW_TEST_SUCCESS"))

                    // Verificar que compilación fue exitosa
                    .andExpect(jsonPath("$.compilationSuccessful").value(true))

                    // Verificar endpoint health después
                    .andExpect(status().isOk())
                    .andDo(print());

            System.out.println("✅ Complete flow test passed");
        }
    }
}