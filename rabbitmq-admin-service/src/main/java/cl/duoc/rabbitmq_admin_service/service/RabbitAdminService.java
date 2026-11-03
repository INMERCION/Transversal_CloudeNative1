package cl.duoc.rabbitmq_admin_service.service;

import cl.duoc.rabbitmq_admin_service.dto.QueueRequest;
import cl.duoc.rabbitmq_admin_service.dto.ExchangeRequest;
import cl.duoc.rabbitmq_admin_service.dto.BindingRequest;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.AmqpException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;
    private final RabbitManagementService managementService;

    public RabbitAdminService(
            RabbitAdmin rabbitAdmin,
            RabbitManagementService managementService) {

        this.rabbitAdmin = rabbitAdmin;
        this.managementService = managementService;
    }

    // =====================================================
    // VALIDACIONES GENERALES
    // =====================================================

    private void validarNombre(String nombre) {

        if (nombre == null ||
                !nombre.matches("^[a-zA-Z0-9._-]{1,100}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de recurso inválido");
        }
    }

    private void protegerCola(String nombre) {

        if (nombre.equals("auditoria.pedidos.queue") ||
                nombre.equals("auditoria.pedidos.dlq")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se permite modificar una cola protegida");
        }
    }

    private void protegerExchange(String nombre) {

        if (nombre.startsWith("amq.") ||
                nombre.equals("pedidos.exchange") ||
                nombre.equals("auditoria.dlx")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se permite modificar un exchange protegido");
        }
    }

    // =====================================================
    // ADMINISTRACIÓN DE COLAS
    // =====================================================

    public void crearCola(QueueRequest request) {

        validarNombre(request.name());
        protegerCola(request.name());

        Queue queue = new Queue(request.name(), true);

        try {
            rabbitAdmin.declareQueue(queue);

        } catch (AmqpException ex) {
            throw errorRabbitMQ(ex);
        }
    }

    public void eliminarCola(String nombre) {

        validarNombre(nombre);
        protegerCola(nombre);

        try {
            if (rabbitAdmin.getQueueProperties(nombre) == null) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cola no existe: " + nombre);
            }

            boolean eliminada = rabbitAdmin.deleteQueue(nombre);

            if (!eliminada) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo eliminar la cola");
            }

        } catch (AmqpException ex) {
            throw errorRabbitMQ(ex);
        }
    }

    // =====================================================
    // ADMINISTRACIÓN DE EXCHANGES
    // =====================================================

    public void crearExchange(ExchangeRequest request) {

        validarNombre(request.name());
        protegerExchange(request.name());

        Exchange exchange;

        switch (request.type().toLowerCase()) {

            case "direct":
                exchange = new DirectExchange(
                        request.name(), true, false);
                break;

            case "topic":
                exchange = new TopicExchange(
                        request.name(), true, false);
                break;

            case "fanout":
                exchange = new FanoutExchange(
                        request.name(), true, false);
                break;

            default:
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tipo inválido. Usa direct, topic o fanout");
        }

        try {
            rabbitAdmin.declareExchange(exchange);

        } catch (AmqpException ex) {
            throw errorRabbitMQ(ex);
        }
    }

    public void eliminarExchange(String nombre) {

        validarNombre(nombre);
        protegerExchange(nombre);

        // Consultar existencia mediante RabbitMQ Management.
        if (!managementService.existeExchange(nombre)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El exchange no existe: " + nombre);
        }

        try {
            boolean eliminado = rabbitAdmin.deleteExchange(nombre);

            if (!eliminado) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo eliminar el exchange");
            }

        } catch (AmqpException ex) {
            throw errorRabbitMQ(ex);
        }
    }

    // =====================================================
    // ADMINISTRACIÓN DE BINDINGS
    // =====================================================

    private void validarBinding(BindingRequest request) {

        validarNombre(request.exchange());
        validarNombre(request.queue());

        // Por seguridad, durante las pruebas solo se
        // permiten bindings de recursos de laboratorio.
        if (!request.exchange().startsWith("prueba.admin.") ||
                !request.queue().startsWith("prueba.admin.")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo se permiten bindings de prueba");
        }

        if (rabbitAdmin.getQueueProperties(request.queue()) == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La cola no existe: " + request.queue());
        }

        if (!managementService.existeExchange(request.exchange())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El exchange no existe: " + request.exchange());
        }
    }

    public void crearBinding(BindingRequest request) {

        try {
            validarBinding(request);

            Binding binding = new Binding(
                    request.queue(),
                    Binding.DestinationType.QUEUE,
                    request.exchange(),
                    request.routingKey(),
                    null
            );

            rabbitAdmin.declareBinding(binding);

        } catch (AmqpException ex) {
            throw errorRabbitMQ(ex);
        }
    }

    public void eliminarBinding(BindingRequest request) {

        try {
            validarBinding(request);

            Binding binding = new Binding(
                    request.queue(),
                    Binding.DestinationType.QUEUE,
                    request.exchange(),
                    request.routingKey(),
                    null
            );

            rabbitAdmin.removeBinding(binding);

        } catch (AmqpException ex) {
            throw errorRabbitMQ(ex);
        }
    }

    // =====================================================
    // MANEJO DE ERRORES DEL BROKER
    // =====================================================

    private ResponseStatusException errorRabbitMQ(AmqpException ex) {

        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo completar la operación en RabbitMQ",
                ex);
    }
}
