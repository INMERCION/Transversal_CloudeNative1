package cl.duoc.auditoria_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record EventoAuditoria(

        @NotBlank(message = "El ID del evento es obligatorio")
        String eventoId,

        @NotBlank(message = "El tipo de evento es obligatorio")
        String tipoEvento,

        @NotBlank(message = "El ID del usuario es obligatorio")
        String usuarioId,

        String email,

        @NotNull(message = "El ID del pedido es obligatorio")
        @Positive(message = "El ID del pedido debe ser positivo")
        Long pedidoId,

        @NotNull(message = "La fecha del evento es obligatoria")
        LocalDateTime fecha,

        @NotBlank(message = "El servicio es obligatorio")
        String servicio

) {}