package cl.duoc.pedidos360_backend.config;

public final class RabbitNames {
    private RabbitNames() {}

    public static final String EXCHANGE = "pedidos.exchange";
    public static final String RK_CREADO = "pedido.creado";
    public static final String RK_ACTUALIZADO = "pedido.actualizado";
    public static final String RK_ELIMINADO = "pedido.eliminado";
}