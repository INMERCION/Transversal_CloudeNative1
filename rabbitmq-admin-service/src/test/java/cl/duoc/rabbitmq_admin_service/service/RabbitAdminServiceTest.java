package cl.duoc.rabbitmq_admin_service.service;

import cl.duoc.rabbitmq_admin_service.dto.QueueRequest;
import cl.duoc.rabbitmq_admin_service.dto.ExchangeRequest;
import cl.duoc.rabbitmq_admin_service.dto.BindingRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @Mock
    private RabbitManagementService managementService;

    @InjectMocks
    private RabbitAdminService service;

    // =====================================================
    // PRUEBAS DE COLAS
    // =====================================================

    @Test
    void crearColaValida() {

        QueueRequest request =
                new QueueRequest("prueba.admin.queue");

        service.crearCola(request);

        ArgumentCaptor<Queue> captor =
                ArgumentCaptor.forClass(Queue.class);

        verify(rabbitAdmin).declareQueue(captor.capture());

        Queue cola = captor.getValue();

        assertEquals("prueba.admin.queue", cola.getName());
        assertTrue(cola.isDurable());
    }

    @Test
    void rechazarCreacionColaProtegida() {

        QueueRequest request =
                new QueueRequest("auditoria.pedidos.queue");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.crearCola(request)
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                ex.getStatusCode()
        );

        verify(rabbitAdmin, never())
                .declareQueue(any(Queue.class));
    }

    @Test
    void eliminarColaInexistente() {

        when(rabbitAdmin.getQueueProperties(
                "prueba.inexistente"))
                .thenReturn(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminarCola(
                        "prueba.inexistente")
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                ex.getStatusCode()
        );

        verify(rabbitAdmin, never())
                .deleteQueue(anyString());
    }

    @Test
    void eliminarColaExistente() {

        when(rabbitAdmin.getQueueProperties(
                "prueba.admin.queue"))
                .thenReturn(new Properties());

        when(rabbitAdmin.deleteQueue(
                "prueba.admin.queue"))
                .thenReturn(true);

        service.eliminarCola("prueba.admin.queue");

        verify(rabbitAdmin)
                .deleteQueue("prueba.admin.queue");
    }

    // =====================================================
    // PRUEBAS DE EXCHANGES
    // =====================================================

    @Test
    void crearExchangeTopic() {

        ExchangeRequest request =
                new ExchangeRequest(
                        "prueba.admin.exchange",
                        "topic"
                );

        service.crearExchange(request);

        ArgumentCaptor<TopicExchange> captor =
                ArgumentCaptor.forClass(TopicExchange.class);

        verify(rabbitAdmin)
                .declareExchange(captor.capture());

        assertEquals(
                "prueba.admin.exchange",
                captor.getValue().getName()
        );

        assertTrue(captor.getValue().isDurable());
    }

    @Test
    void rechazarTipoExchangeInvalido() {

        ExchangeRequest request =
                new ExchangeRequest(
                        "prueba.admin.exchange",
                        "invalido"
                );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.crearExchange(request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verify(rabbitAdmin, never())
                .declareExchange(any());
    }

    @Test
    void rechazarEliminacionExchangeProtegido() {

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminarExchange(
                        "pedidos.exchange")
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                ex.getStatusCode()
        );

        verify(rabbitAdmin, never())
                .deleteExchange(anyString());

        verifyNoInteractions(managementService);
    }

    @Test
    void eliminarExchangeInexistente() {

        when(managementService.existeExchange(
                "prueba.inexistente"))
                .thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminarExchange(
                        "prueba.inexistente")
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                ex.getStatusCode()
        );

        verify(rabbitAdmin, never())
                .deleteExchange(anyString());
    }

    // =====================================================
    // PRUEBAS DE BINDINGS
    // =====================================================

    @Test
    void crearBindingValido() {

        BindingRequest request = new BindingRequest(
                "prueba.admin.exchange",
                "prueba.admin.queue",
                "pedido.creado"
        );

        when(rabbitAdmin.getQueueProperties(
                request.queue()))
                .thenReturn(new Properties());

        when(managementService.existeExchange(
                request.exchange()))
                .thenReturn(true);

        service.crearBinding(request);

        ArgumentCaptor<Binding> captor =
                ArgumentCaptor.forClass(Binding.class);

        verify(rabbitAdmin)
                .declareBinding(captor.capture());

        Binding binding = captor.getValue();

        assertEquals(
                "prueba.admin.queue",
                binding.getDestination()
        );

        assertEquals(
                "prueba.admin.exchange",
                binding.getExchange()
        );

        assertEquals(
                "pedido.creado",
                binding.getRoutingKey()
        );
    }

    @Test
    void eliminarBindingInexistente() {

        BindingRequest request = new BindingRequest(
                "prueba.admin.exchange",
                "prueba.admin.queue",
                "pedido.creado"
        );

        when(rabbitAdmin.getQueueProperties(
                request.queue()))
                .thenReturn(new Properties());

        when(managementService.existeExchange(
                request.exchange()))
                .thenReturn(true);

        when(managementService.existeBinding(
                request.exchange(),
                request.queue(),
                request.routingKey()))
                .thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminarBinding(request)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                ex.getStatusCode()
        );

        verify(rabbitAdmin, never())
                .removeBinding(any(Binding.class));
    }

    @Test
    void eliminarBindingExistente() {

        BindingRequest request = new BindingRequest(
                "prueba.admin.exchange",
                "prueba.admin.queue",
                "pedido.creado"
        );

        when(rabbitAdmin.getQueueProperties(
                request.queue()))
                .thenReturn(new Properties());

        when(managementService.existeExchange(
                request.exchange()))
                .thenReturn(true);

        when(managementService.existeBinding(
                request.exchange(),
                request.queue(),
                request.routingKey()))
                .thenReturn(true);

        service.eliminarBinding(request);

        verify(rabbitAdmin)
                .removeBinding(any(Binding.class));
    }
}
