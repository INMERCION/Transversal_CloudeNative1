package cl.duoc.rabbitmq_admin_service.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RabbitManagementService {

    private final RestClient client;
    private final String vhost;
    private final String baseUrl;

    public RabbitManagementService(
            @Value("${app.rabbitmq.management-url}") String url,
            @Value("${app.rabbitmq.management-username}") String username,
            @Value("${app.rabbitmq.management-password}") String password,
            @Value("${app.rabbitmq.vhost:/}") String vhost) {

        this.vhost = vhost;
        this.baseUrl = url.replaceAll("/+$", "");

        var httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.client = RestClient.builder()
                .requestFactory(factory)
                .defaultHeaders(headers ->
                        headers.setBasicAuth(username, password))
                .build();
    }

    // =====================================================
    // CONSULTAR EXISTENCIA DE EXCHANGE
    // =====================================================

    public boolean existeExchange(String nombre) {

        String ruta = "/api/exchanges/"
                + codificar(vhost)
                + "/"
                + codificar(nombre);

        try {
            client.get()
                    .uri(URI.create(baseUrl + ruta))
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (HttpClientErrorException.NotFound ex) {
            return false;

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar RabbitMQ Management",
                    ex);
        }
    }

    // =====================================================
    // CONSULTAR EXISTENCIA DE BINDING
    // =====================================================

    public boolean existeBinding(
            String exchange,
            String queue,
            String routingKey) {

        String ruta = "/api/bindings/"
                + codificar(vhost)
                + "/e/"
                + codificar(exchange)
                + "/q/"
                + codificar(queue);

        try {
            List<Map<String, Object>> bindings = client.get()
                    .uri(URI.create(baseUrl + ruta))
                    .retrieve()
                    .body(
                        new ParameterizedTypeReference<
                            List<Map<String, Object>>>() {}
                    );

            if (bindings == null) {
                return false;
            }

            return bindings.stream().anyMatch(binding -> {

                Object clave = binding.get("routing_key");
                Object argumentos = binding.get("arguments");

                boolean claveCorrecta =
                        routingKey.equals(clave);

                boolean sinArgumentos =
                        argumentos instanceof Map<?, ?> mapa
                        && mapa.isEmpty();

                return claveCorrecta && sinArgumentos;
            });

        } catch (HttpClientErrorException.NotFound ex) {
            return false;

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar los bindings de RabbitMQ",
                    ex);
        }
    }

    // =====================================================
    // CODIFICAR NOMBRES PARA URL
    // =====================================================

    private String codificar(String valor) {
        return URLEncoder.encode(
                valor,
                StandardCharsets.UTF_8);
    }
}
