package cl.duoc.rabbitmq_admin_service.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
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

    // Verifica si un exchange existe mediante
    // la RabbitMQ Management HTTP API.
    public boolean existeExchange(String nombre) {

        String ruta = "/api/exchanges/"
                + codificar(vhost)
                + "/"
                + codificar(nombre);

        URI uri = URI.create(baseUrl + ruta);

        try {

            client.get()
                    .uri(uri)
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

    private String codificar(String valor) {

        return URLEncoder.encode(
                valor,
                StandardCharsets.UTF_8);
    }
}
