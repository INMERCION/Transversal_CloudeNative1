package cl.duoc.auditoria_service.repository;

import cl.duoc.auditoria_service.model.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

}
