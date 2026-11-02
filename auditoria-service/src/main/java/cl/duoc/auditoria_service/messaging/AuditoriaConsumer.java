
package cl.duoc.auditoria_service.messaging;

import cl.duoc.auditoria_service.config.RabbitMQConfig;
import cl.duoc.auditoria_service.dto.EventoAuditoria;
import cl.duoc.auditoria_service.service.AuditoriaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@Component
@Validated
public class AuditoriaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AuditoriaConsumer.class);

    private final AuditoriaService service;
    private final SpringValidatorAdapter validator;

    public AuditoriaConsumer(
            AuditoriaService service,
            SpringValidatorAdapter validator) {
        this.service = service;
        this.validator = validator;
    }

    @RabbitListener(queues = RabbitMQConfig.AUDITORIA_QUEUE)
    public void recibirEvento(EventoAuditoria evento) {

        BindingResult resultado = new BeanPropertyBindingResult(
                evento, "eventoAuditoria");

        validator.validate(evento, resultado);

        if (resultado.hasErrors()) {
            throw new IllegalArgumentException(
                    "Evento de auditoría inválido: " +
                    resultado.getAllErrors());
        }

        String usuario = evento.email() != null
                && !evento.email().isBlank()
                ? evento.email()
                : evento.usuarioId();

        String descripcion =
                "Evento " + evento.tipoEvento()
                + " generado por " + evento.servicio()
                + ". ID del evento: " + evento.eventoId();

        log.info("Evento recibido: {}", evento.tipoEvento());

        service.registrar(
                evento.tipoEvento(),
                evento.pedidoId(),
                usuario,
                descripcion,
                evento.fecha()
        );

        log.info("Evento de auditoría guardado correctamente");
    }
}
