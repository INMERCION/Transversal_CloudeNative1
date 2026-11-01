
package cl.duoc.auditoria_service.controller;

import cl.duoc.auditoria_service.model.RegistroAuditoria;
import cl.duoc.auditoria_service.service.AuditoriaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    // Obtener todos los registros de auditoría
    @GetMapping
    public List<RegistroAuditoria> listar() {
        return service.listar();
    }

    // Obtener un registro específico por su ID
    @GetMapping("/{id}")
    public ResponseEntity<RegistroAuditoria> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }
}
