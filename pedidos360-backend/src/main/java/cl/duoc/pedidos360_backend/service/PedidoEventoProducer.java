package cl.duoc.pedidos360_backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import cl.duoc.pedidos360_backend.config.RabbitNames;
import cl.duoc.pedidos360_backend.model.PedidoEvento;

@Service
public class PedidoEventoProducer {

    private final RabbitTemplate rabbitTemplate;

    public PedidoEventoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(String tipoEvento, String routingKey, Long pedidoId, Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            if (email == null) email = jwt.getClaimAsString("username");

            PedidoEvento evento = new PedidoEvento(
                    UUID.randomUUID().toString(),
                    tipoEvento,
                    jwt.getSubject(),
                    email,
                    pedidoId,
                    LocalDateTime.now(),
                    "pedidos360-backend");

            rabbitTemplate.convertAndSend(RabbitNames.EXCHANGE, routingKey, evento);
            System.out.println("[PRODUCER] Evento publicado: " + tipoEvento + " pedidoId=" + pedidoId);
        } catch (RuntimeException ex) {
            // La auditoría no debe impedir que el pedido se guarde
            System.out.println("[PRODUCER] No se pudo publicar " + tipoEvento + ": " + ex.getMessage());
        }
    }
}