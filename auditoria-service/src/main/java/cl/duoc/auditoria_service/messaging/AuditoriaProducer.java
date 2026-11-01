
package cl.duoc.auditoria_service.messaging;

import cl.duoc.auditoria_service.config.RabbitMQConfig;
import cl.duoc.auditoria_service.dto.EventoAuditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaProducer {

    private static final Logger log =
            LoggerFactory.getLogger(AuditoriaProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public AuditoriaProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Publica un evento en RabbitMQ
    public void publicarEvento(EventoAuditoria evento) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVENTOS_EXCHANGE,
                "auditoria.registrada",
                evento
        );

        log.info("Evento de auditoría publicado: {}", evento.tipoEvento());
    }
}
