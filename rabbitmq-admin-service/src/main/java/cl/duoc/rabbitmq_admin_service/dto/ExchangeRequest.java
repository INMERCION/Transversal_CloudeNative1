package cl.duoc.rabbitmq_admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]{1,100}$",
        message = "Nombre de exchange inválido"
    )
    String name,

    @NotBlank(message = "El tipo de exchange es obligatorio")
    String type

) {}

