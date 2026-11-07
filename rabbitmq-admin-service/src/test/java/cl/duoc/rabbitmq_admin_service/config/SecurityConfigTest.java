package cl.duoc.rabbitmq_admin_service.config;

import cl.duoc.rabbitmq_admin_service.controller.RabbitAdminController;
import cl.duoc.rabbitmq_admin_service.dto.QueueRequest;
import cl.duoc.rabbitmq_admin_service.service.RabbitAdminService;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RabbitAdminController.class)
@Import({
    SecurityConfig.class,
    SecurityConfigTest.TestSecurityConfiguration.class
})

class SecurityConfigTest {
        @TestConfiguration(proxyBeanMethods = false)
        @EnableWebSecurity
        static class TestSecurityConfiguration {
        }

    private static final String URL = "/api/admin/queues";

    private static final String BODY = """
            {
              "name": "prueba.seguridad.queue"
            }
            """;

    private static final String SCOPE =
            "SCOPE_rs-api-rabbitmq/admin";

    @Autowired
    private MockMvc mockMvc;

    // Simular la lógica del administrador.
    @MockitoBean
    private RabbitAdminService rabbitAdminService;

    // Simular la validación criptográfica del JWT.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    // =====================================================
    // TEST 1: SOLICITUD SIN TOKEN
    // =====================================================

    @Test
    void solicitudSinTokenDebeRetornar401() throws Exception {

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rabbitAdminService);
    }

    // =====================================================
    // TEST 2: TOKEN INVÁLIDO
    // =====================================================

    @Test
    void tokenInvalidoDebeRetornar401() throws Exception {

        when(jwtDecoder.decode("token-invalido"))
                .thenThrow(new BadJwtException(
                        "Token JWT inválido"));

        mockMvc.perform(post(URL)
                        .header("Authorization",
                                "Bearer token-invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rabbitAdminService);
    }

    // =====================================================
    // TEST 3: USUARIO ADMINISTRADOR AUTORIZADO
    // =====================================================

    @Test
    void administradorAutorizadoDebeRetornar201()
            throws Exception {

        mockMvc.perform(post(URL)
                        .with(jwt()
                                .jwt(token -> token
                                        .claim(
                                            "cognito:groups",
                                            List.of("RabbitMQAdmins"))
                                        .claim(
                                            "scope",
                                            "rs-api-rabbitmq/admin"))
                                .authorities(
                                    new SimpleGrantedAuthority(SCOPE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(content().string(
                        "Cola creada correctamente: prueba.seguridad.queue"));

        verify(rabbitAdminService)
                .crearCola(any(QueueRequest.class));
    }

    // =====================================================
    // TEST 4: USUARIO CON SCOPE PERO SIN GRUPO
    // =====================================================

    @Test
    void usuarioSinGrupoDebeRetornar403()
            throws Exception {

        mockMvc.perform(post(URL)
                        .with(jwt()
                                .jwt(token -> token
                                        .claim(
                                            "cognito:groups",
                                            List.of("Usuarios"))
                                        .claim(
                                            "scope",
                                            "rs-api-rabbitmq/admin"))
                                .authorities(
                                    new SimpleGrantedAuthority(SCOPE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rabbitAdminService);
    }

    // =====================================================
    // TEST 5: USUARIO CON GRUPO PERO SIN SCOPE
    // =====================================================

    @Test
    void administradorSinScopeDebeRetornar403()
            throws Exception {

        mockMvc.perform(post(URL)
                        .with(jwt()
                                .jwt(token -> token
                                        .claim(
                                            "cognito:groups",
                                            List.of("RabbitMQAdmins"))
                                        .claim(
                                            "scope",
                                            "openid email"))
                                .authorities(
                                    new SimpleGrantedAuthority(
                                            "SCOPE_openid")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rabbitAdminService);
    }

    // =====================================================
    // TEST 6: RUTA BLOQUEADA
    // =====================================================

    @Test
    void rutaNoAutorizadaDebeRetornar403()
            throws Exception {

        mockMvc.perform(get("/api/otro")
                        .with(jwt()
                                .jwt(token -> token
                                        .claim(
                                            "cognito:groups",
                                            List.of("RabbitMQAdmins"))
                                        .claim(
                                            "scope",
                                            "rs-api-rabbitmq/admin"))
                                .authorities(
                                    new SimpleGrantedAuthority(SCOPE))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rabbitAdminService);
    }
}
