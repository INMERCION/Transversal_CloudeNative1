
package cl.duoc.auditoria_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter; //ayuda a convertir los mensajes a JSON
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombres centralizados de RabbitMQ
    public static final String EVENTOS_EXCHANGE = "pedidos360.eventos.exchange";
    public static final String AUDITORIA_QUEUE = "auditoria.pedidos.queue";
    public static final String AUDITORIA_ROUTING_KEY = "pedido.#";

    // Configuración de mensajes fallidos
    public static final String DLX_EXCHANGE = "auditoria.dlx";
    public static final String DLQ_QUEUE = "auditoria.pedidos.dlq";
    public static final String DLQ_ROUTING_KEY = "auditoria.pedidos.dlq";

    // Exchange principal
    @Bean
    public TopicExchange eventosExchange() { //Se usa TopicExchange para permitir el uso de patrones de routing key
        return new TopicExchange(EVENTOS_EXCHANGE, true, false);
    }

    // Exchange para mensajes fallidos
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // Cola principal de auditoría
    @Bean
    public Queue auditoriaQueue() {
        return QueueBuilder.durable(AUDITORIA_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    // Cola de mensajes fallidos
    @Bean
    public Queue auditoriaDlq() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    // Conectar exchange principal con la cola de auditoría
    @Bean
    public Binding auditoriaBinding() {
        return BindingBuilder
                .bind(auditoriaQueue())
                .to(eventosExchange())
                .with(AUDITORIA_ROUTING_KEY);
    }

    // Conectar exchange de errores con la DLQ
    @Bean
    public Binding auditoriaDlqBinding() {
        return BindingBuilder
                .bind(auditoriaDlq())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
