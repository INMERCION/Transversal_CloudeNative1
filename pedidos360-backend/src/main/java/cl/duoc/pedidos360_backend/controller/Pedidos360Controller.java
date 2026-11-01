package cl.duoc.pedidos360_backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.pedidos360_backend.config.RabbitNames;
import cl.duoc.pedidos360_backend.model.Pedido;
import cl.duoc.pedidos360_backend.model.PedidoRequest;
import cl.duoc.pedidos360_backend.service.PedidoEventoProducer;
import cl.duoc.pedidos360_backend.service.PedidoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
public class Pedidos360Controller {
    private final PedidoService service;
    private final PedidoEventoProducer eventos;

    public Pedidos360Controller(PedidoService service, PedidoEventoProducer eventos) {
        this.service = service;
        this.eventos = eventos;
    }

    @GetMapping
    public List<Pedido> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        Pedido pedido = service.crear(request);
        eventos.publicar("PEDIDO_CREADO", RabbitNames.RK_CREADO, pedido.getId(), jwt);
        // Resolves correctly both directly and behind the /test gateway stage.
        return ResponseEntity.created(URI.create("pedidos/" + pedido.getId())).body(pedido);
    }

    @PutMapping("/{id}")
    public Pedido actualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request,
                             @AuthenticationPrincipal Jwt jwt) {
        Pedido pedido = service.actualizar(id, request);
        eventos.publicar("PEDIDO_ACTUALIZADO", RabbitNames.RK_ACTUALIZADO, pedido.getId(), jwt);
        return pedido;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminar(id);
        eventos.publicar("PEDIDO_ELIMINADO", RabbitNames.RK_ELIMINADO, id, jwt);
        return ResponseEntity.noContent().build();
    }
}