package socket;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import util.Json_Util;
import util.Sesion_Util;

/**
 * "Aceptador" de conexiones WebSocket de One More Dish.
 *
 * Es el equivalente a tu ServerSocket del ejemplo de clase, pero Tomcat hace el
 * accept() por nosotros: cada vez que un navegador abre ws://.../ws/pedidos,
 * Tomcat crea una Session y llama a alAbrir().
 *
 * El socket SOLO EMPUJA AVISOS del servidor hacia los navegadores.
 * Las acciones (crear pedido, cambiar estado) siguen yendo por los servlets,
 * que validan el rol y guardan en la base de datos; después llaman a
 * notificarPedidoNuevo() / notificarEstadoPedido().
 *
 * Salas (grupos de conexiones):
 *   "admin"         -> usuarios ADMIN
 *   "cocina"        -> usuarios COCINERO
 *   "meseros"       -> usuarios MESERO (para mesas en vivo, más adelante)
 *   "cliente_<id>"  -> SOLO ese cliente (cada cliente ve únicamente sus pedidos)
 */
@ServerEndpoint(value = "/ws/pedidos", configurator = Configurador_Socket.class)
public class Pedidos_Socket {

    public static final String SALA_ADMIN = "admin";
    public static final String SALA_COCINA = "cocina";
    public static final String SALA_MESEROS = "meseros";

    // sala -> conexiones abiertas en esa sala (estructuras seguras para varios hilos)
    private static final Map<String, Set<Session>> SALAS = new ConcurrentHashMap<>();

    // Si no llega nada (ni siquiera el "ping" del navegador) en 2 minutos, se cierra
    private static final long TIEMPO_INACTIVO_MS = 120000;

    // ================== Ciclo de vida de una conexión ==================

    @OnOpen
    public void alAbrir(Session sesion, EndpointConfig config) throws IOException {
        Object rol = config.getUserProperties().get(Sesion_Util.ATR_ROL);
        Object id = config.getUserProperties().get(Sesion_Util.ATR_ID);

        // Sin sesión de login -> se cierra con código 1008 (el JS ya no reintenta)
        if (!(rol instanceof String) || !(id instanceof Integer)) {
            sesion.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Inicia sesion"));
            return;
        }

        List<String> salas = salasPara((String) rol, (Integer) id);
        if (salas.isEmpty()) {
            sesion.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Rol sin acceso"));
            return;
        }

        sesion.setMaxIdleTimeout(TIEMPO_INACTIVO_MS);
        sesion.getUserProperties().put("salas", salas);

        for (String sala : salas) {
            SALAS.computeIfAbsent(sala, k -> ConcurrentHashMap.newKeySet()).add(sesion);
        }

        enviar(sesion, Json_Util.aJson(Json_Util.mapa(
                "tipo", "bienvenida",
                "rol", rol,
                "salas", salas)));
    }

    // Lo único que acepta del navegador es el "ping" para mantener viva la conexión
    @OnMessage(maxMessageSize = 1024)
    public void alRecibir(Session sesion, String texto) {
        if (texto != null && texto.contains("\"ping\"")) {
            enviar(sesion, "{\"tipo\":\"pong\"}");
        }
    }

    @OnClose
    public void alCerrar(Session sesion) {
        quitar(sesion);
    }

    @OnError
    public void alFallar(Session sesion, Throwable error) {
        // Pasa cuando el navegador se cierra de golpe o se cae el internet; no es grave
        quitar(sesion);
    }

    // ================== Métodos que llaman los servlets ==================

    /** Un cliente creó un pedido: lo ven admin, cocina y el propio cliente. */
    public static void notificarPedidoNuevo(Map<String, Object> pedido, int idCliente) {
        String json = Json_Util.aJson(Json_Util.mapa(
                "tipo", "pedido_nuevo",
                "pedido", pedido));
        enviarASalas(json, SALA_ADMIN, SALA_COCINA, salaCliente(idCliente));
    }

    /** Cambió el estado de un pedido: lo ven admin, cocina y el dueño del pedido. */
    public static void notificarEstadoPedido(int idPedido, String estado, int idCliente) {
        String json = Json_Util.aJson(Json_Util.mapa(
                "tipo", "pedido_estado",
                "id", idPedido,
                "estado", estado));
        enviarASalas(json, SALA_ADMIN, SALA_COCINA, salaCliente(idCliente));
    }

    /** Envía el mismo mensaje a varias salas (sin repetir si alguien está en dos). */
    public static void enviarASalas(String json, String... salas) {
        Set<Session> destinatarios = new HashSet<>();
        for (String sala : salas) {
            if (sala == null) continue;
            Set<Session> conexiones = SALAS.get(sala);
            if (conexiones != null) destinatarios.addAll(conexiones);
        }
        for (Session s : destinatarios) {
            enviar(s, json);
        }
    }

    public static String salaCliente(int idCliente) {
        return idCliente > 0 ? "cliente_" + idCliente : null;
    }

    // ================== Ayudas privadas ==================

    private static List<String> salasPara(String rol, int id) {
        List<String> salas = new ArrayList<>();
        switch (rol) {
            case Sesion_Util.ADMIN:    salas.add(SALA_ADMIN); break;
            case Sesion_Util.COCINERO: salas.add(SALA_COCINA); break;
            case Sesion_Util.MESERO:   salas.add(SALA_MESEROS); break;
            case Sesion_Util.CLIENTE:  salas.add(salaCliente(id)); break;
            default: break; // PROVEEDOR u otro: no recibe avisos de pedidos
        }
        salas.remove(null);
        return salas;
    }

    private static void enviar(Session sesion, String json) {
        if (!sesion.isOpen()) {
            quitar(sesion);
            return;
        }
        try {
            // getBasicRemote() no permite dos envíos a la vez a la misma conexión;
            // el synchronized hace que esperen su turno.
            synchronized (sesion) {
                sesion.getBasicRemote().sendText(json);
            }
        } catch (IOException | IllegalStateException e) {
            quitar(sesion);
        }
    }

    @SuppressWarnings("unchecked")
    private static void quitar(Session sesion) {
        Object salas = sesion.getUserProperties().get("salas");
        if (salas instanceof List) {
            for (String sala : (List<String>) salas) {
                Set<Session> conexiones = SALAS.get(sala);
                if (conexiones != null) conexiones.remove(sesion);
            }
        }
    }
}
