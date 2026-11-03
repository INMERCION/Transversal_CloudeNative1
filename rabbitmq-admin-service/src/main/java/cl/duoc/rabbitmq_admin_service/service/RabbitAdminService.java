package cl.duoc.rabbitmq_admin_service.service;

import cl.duoc.rabbitmq_admin_service.dto.QueueRequest;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;
import cl.duoc.rabbitmq_admin_service.dto.ExchangeRequest;
import org.springframework.amqp.core.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.rabbitmq_admin_service.dto.BindingRequest;

@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    //Este método crea una cola en RabbitMQ utilizando el nombre proporcionado en el objeto QueueRequest. La cola se declara como duradera (true) para que persista incluso después de reiniciar RabbitMQ.
    public void crearCola(QueueRequest request) {
        Queue queue = new Queue(request.name(), true);
        rabbitAdmin.declareQueue(queue);
    }

    //Este método elimina una cola de RabbitMQ. Antes de eliminar, verifica que el nombre de la cola sea válido y que no sea una cola protegida. Si la cola no existe o no puede eliminarse, lanza una excepción con un estado HTTP 404.   
    public void eliminarCola(String nombre) {

        // 1. Validar el nombre
        if (nombre == null ||
                !nombre.matches("^[a-zA-Z0-9._-]{1,100}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de cola inválido");
        }

        // 2. Proteger las colas de auditoría
        if (nombre.equals("auditoria.pedidos.queue") ||
                nombre.equals("auditoria.pedidos.dlq")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se permite eliminar una cola protegida");
        }

        // 3. Comprobar si la cola existe
        if (rabbitAdmin.getQueueProperties(nombre) == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La cola no existe: " + nombre);
        }

        // 4. Eliminar la cola
        boolean eliminada = rabbitAdmin.deleteQueue(nombre);

        if (!eliminada) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo eliminar la cola");
        }
    }



    // Este método crea un exchange en RabbitMQ utilizando el nombre y tipo proporcionados en el objeto ExchangeRequest. Dependiendo del tipo de exchange (direct, topic o fanout), se crea la instancia correspondiente y se declara como duradera (true) para que persista incluso después de reiniciar RabbitMQ. Si el tipo de exchange no es válido, lanza una excepción.
    public void crearExchange(ExchangeRequest request) {

        Exchange exchange;

        switch (request.type().toLowerCase()) {
            case "direct":
                exchange = new DirectExchange(request.name(), true, false);
                break;

            case "topic":
                exchange = new TopicExchange(request.name(), true, false);
                break;

            case "fanout":
                exchange = new FanoutExchange(request.name(), true, false);
                break;

            default:
                throw new IllegalArgumentException(
                    "Tipo de exchange no permitido. Usa direct, topic o fanout");
        }

        rabbitAdmin.declareExchange(exchange);
    }

    // Este método elimina un exchange de RabbitMQ. Antes de eliminar, verifica que el nombre del exchange sea válido y que no sea un exchange protegido. Si el exchange no existe o no puede eliminarse, lanza una excepción con un estado HTTP 404.
    
    public void eliminarExchange(String nombre) {

        // Validar nombre del exchange
        if (nombre == null ||
                !nombre.matches("^[a-zA-Z0-9._-]{1,100}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de exchange inválido");
        }

        // Proteger exchanges del sistema
        if (nombre.startsWith("amq.") ||
                nombre.equals("pedidos.exchange") ||
                nombre.equals("auditoria.dlx")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se permite eliminar un exchange protegido: " + nombre);
        }

        // Eliminar exchange
        boolean eliminado = rabbitAdmin.deleteExchange(nombre);

        if (!eliminado) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El exchange no existe o no pudo eliminarse");
        }
    }

    // Este método valida un objeto BindingRequest para asegurarse de que los recursos a los que se hace referencia (exchange y queue) cumplan con ciertas condiciones. Verifica que los nombres de exchange y queue comiencen con "prueba.admin." para permitir solo bindings de prueba, y también comprueba que la cola especificada exista en RabbitMQ. Si alguna de estas condiciones no se cumple, lanza una excepción con el estado HTTP correspondiente.
    private void validarBinding(BindingRequest request) {

        // Solo permitir cambios en recursos de laboratorio
        // durante esta etapa de desarrollo.
        if (!request.exchange().startsWith("prueba.admin.") ||
            !request.queue().startsWith("prueba.admin.")) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Solo se permiten bindings de prueba");
        }

        // Comprobar que la cola existe.
        if (rabbitAdmin.getQueueProperties(request.queue()) == null) {

            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "La cola indicada no existe");
        }
    }

    public void crearBinding(BindingRequest request) {

        validarBinding(request);

        Binding binding = new Binding(
            request.queue(),
            Binding.DestinationType.QUEUE,
            request.exchange(),
            request.routingKey(),
            null
        );

        rabbitAdmin.declareBinding(binding);
    }

    public void eliminarBinding(BindingRequest request) {

        validarBinding(request);

        Binding binding = new Binding(
            request.queue(),
            Binding.DestinationType.QUEUE,
            request.exchange(),
            request.routingKey(),
            null
        );

        rabbitAdmin.removeBinding(binding);
    }

}
