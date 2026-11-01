package cl.duoc.pedidos360_backend.model;

import java.time.LocalDateTime;

public record PedidoEvento(
        String eventoId,
        String tipoEvento,
        String usuarioId,
        String email,
        Long pedidoId,
        LocalDateTime fecha,
        String servicio) {
}