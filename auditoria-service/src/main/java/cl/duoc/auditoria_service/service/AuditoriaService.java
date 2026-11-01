
package cl.duoc.auditoria_service.service;

import cl.duoc.auditoria_service.model.RegistroAuditoria;
import cl.duoc.auditoria_service.repository.AuditoriaRepository;
import cl.duoc.auditoria_service.dto.EventoAuditoria;
import cl.duoc.auditoria_service.messaging.AuditoriaProducer;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Consultar todos los registros
    public List<RegistroAuditoria> listar() {
        return repository.findAll();
    }

    // Consultar un registro por ID
    public RegistroAuditoria obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Registro de auditoría no encontrado"));
    }

    // Registrar un evento de auditoría
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
                fecha
        );

        RegistroAuditoria guardado = repository.save(registro);

        // Publicar evento indicando que la auditoría fue registrada
        EventoAuditoria evento = new EventoAuditoria(
                "AUDITORIA_REGISTRADA",
                guardado.getPedidoId(),
                guardado.getUsuario(),
                "Se registró el evento: " + guardado.getTipoEvento(),
                guardado.getFecha()
        );

        producer.publicarEvento(evento);

        return guardado;
    }
}
