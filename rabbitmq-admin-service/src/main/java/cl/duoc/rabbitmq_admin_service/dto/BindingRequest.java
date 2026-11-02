package cl.duoc.rabbitmq_admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record BindingRequest(

    @NotBlank(message = "El exchange es obligatorio")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]{1,100}$",
        message = "Nombre de exchange inválido"
    )
    String exchange,

    @NotBlank(message = "La cola es obligatoria")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]{1,100}$",
        message = "Nombre de cola inválido"
    )
    String queue,

    @NotBlank(message = "La routing key es obligatoria")
    @Pattern(
        regexp = "^[a-zA-Z0-9.*#-]{1,100}$",
        message = "Routing key inválida"
    )
    String routingKey

) {}
