package cl.duoc.rabbitmq_admin_service.controller;

import cl.duoc.rabbitmq_admin_service.dto.QueueRequest;
import cl.duoc.rabbitmq_admin_service.service.RabbitAdminService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cl.duoc.rabbitmq_admin_service.dto.ExchangeRequest;
import cl.duoc.rabbitmq_admin_service.dto.BindingRequest;

@RestController
@RequestMapping("/api/admin")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    // Este método maneja la solicitud POST para crear una cola en RabbitMQ. Recibe un objeto QueueRequest que contiene el nombre de la cola a crear. Valida el objeto de solicitud y llama al servicio para crear la cola. Devuelve una respuesta HTTP 201 (CREATED) con un mensaje de éxito.
    @PostMapping("/queues")
    public ResponseEntity<String> crearCola(
            @Valid @RequestBody QueueRequest request) {

        service.crearCola(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Cola creada correctamente: " + request.name());
    }

    // Este método maneja la solicitud DELETE para eliminar una cola en RabbitMQ. Recibe el nombre de la cola a eliminar como un parámetro de ruta. Llama al servicio para eliminar la cola y devuelve una respuesta HTTP 200 (OK) con un mensaje de éxito. Si la cola no existe o no puede eliminarse, se lanza una excepción que se traduce en un estado HTTP 404.
    @DeleteMapping("/queues/{name}")
    public ResponseEntity<String> eliminarCola(@PathVariable String name) {
        service.eliminarCola(name);
        return ResponseEntity.ok("Cola eliminada correctamente: " + name);
    }

    // Este método maneja la solicitud POST para crear un exchange en RabbitMQ. Recibe un objeto ExchangeRequest que contiene el nombre y tipo del exchange a crear. Valida el objeto de solicitud y llama al servicio para crear el exchange. Devuelve una respuesta HTTP 201 (CREATED) con un mensaje de éxito.
    @PostMapping("/exchanges")
    public ResponseEntity<String> crearExchange(
            @Valid @RequestBody ExchangeRequest request) {

        service.crearExchange(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Exchange creado correctamente: " + request.name());
    }

    // Este método maneja la solicitud DELETE para eliminar un exchange en RabbitMQ. Recibe el nombre del exchange a eliminar como un parámetro de ruta. Llama al servicio para eliminar el exchange y devuelve una respuesta HTTP 204 (NO CONTENT) si la eliminación es exitosa. Si el exchange no existe o no puede eliminarse, se lanza una excepción que se traduce en un estado HTTP 404.
    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> eliminarExchange(
            @PathVariable String name) {

        service.eliminarExchange(name);

        return ResponseEntity.noContent().build();
    }

    // Este método maneja la solicitud POST para crear un binding en RabbitMQ. Recibe un objeto BindingRequest que contiene el nombre del exchange, el nombre de la cola y la routing key para el binding. Valida el objeto de solicitud y llama al servicio para crear el binding. Devuelve una respuesta HTTP 201 (CREATED) con un mensaje de éxito.
    @PostMapping("/bindings")
    public ResponseEntity<String> crearBinding(
            @Valid @RequestBody BindingRequest request) {

        service.crearBinding(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Binding creado correctamente");
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(
            @Valid @RequestBody BindingRequest request) {

        service.eliminarBinding(request);

        return ResponseEntity.noContent().build();
    }



}
