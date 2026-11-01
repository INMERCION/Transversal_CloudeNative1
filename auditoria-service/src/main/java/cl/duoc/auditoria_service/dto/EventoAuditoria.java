
package cl.duoc.auditoria_service.dto;

import jakarta.validation.constraints.NotBlank; //agregamos validaciones para evitar campos en blanco o datos inválidos
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventoAuditoria(

        @NotBlank(message = "El tipo de evento es obligatorio")
        String tipoEvento,

        @NotNull(message = "El ID del pedido es obligatorio")
        @Positive(message = "El ID del pedido debe ser positivo")
        Long pedidoId,

        @NotBlank(message = "El usuario es obligatorio")
        String usuario,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        String descripcion,

        @NotNull(message = "La fecha del evento es obligatoria")
        LocalDateTime fecha

) {}
