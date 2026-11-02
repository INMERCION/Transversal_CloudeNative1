package cl.duoc.auditoria_service.service;

import cl.duoc.auditoria_service.model.RegistroAuditoria;
import cl.duoc.auditoria_service.repository.AuditoriaRepository;
import cl.duoc.auditoria_service.dto.EventoAuditoria;
import cl.duoc.auditoria_service.messaging.AuditoriaProducer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditoriaService {

    private final AuditoriaRepository repository;
    private final AuditoriaProducer producer;

    public AuditoriaService(
            AuditoriaRepository repository,
            AuditoriaProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public List<RegistroAuditoria> listar() {
        return repository.findAll();
    }

    public RegistroAuditoria obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Registro de auditoría no encontrado"));
    }

    @Transactional
    public RegistroAuditoria registrar(
            String tipoEvento,
            Long pedidoId,
            String usuario,
            String descripcion,
            LocalDateTime fecha) {

        RegistroAuditoria registro = new RegistroAuditoria(
                tipoEvento,
                pedidoId,
                usuario,
                descripcion,
                fecha);

        RegistroAuditoria guardado = repository.save(registro);

        EventoAuditoria evento = new EventoAuditoria(
                java.util.UUID.randomUUID().toString(),
                "AUDITORIA_REGISTRADA",
                guardado.getUsuario(),
                guardado.getUsuario(),
                guardado.getPedidoId(),
                guardado.getFecha(),
                "auditoria-service"
        );

        producer.publicarEvento(evento);

        return guardado;
    }
}