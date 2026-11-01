package cl.duoc.auditoria_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoEvento;

    private Long pedidoId;

    private String usuario;

    @Column(length = 1000)
    private String descripcion;

    private LocalDateTime fecha;

    protected RegistroAuditoria() {
    }

    public RegistroAuditoria(
        String tipoEvento,
        Long pedidoId,
        String usuario,
        String descripcion,
        LocalDateTime fecha) {

        this.tipoEvento = tipoEvento;
        this.pedidoId = pedidoId;
        this.usuario = usuario;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}