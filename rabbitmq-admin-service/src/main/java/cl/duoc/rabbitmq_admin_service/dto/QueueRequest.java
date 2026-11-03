package cl.duoc.rabbitmq_admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QueueRequest(

    @NotBlank(message = "El nombre de la cola es obligatorio")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]{1,100}$",
        message = "El nombre solo puede contener letras, numeros, puntos, guiones y guiones bajos"
    )
    String name

) {}

