package socket;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import util.Sesion_Util;

/**
 * Se ejecuta UNA vez por cada conexión nueva, justo durante el "apretón de manos"
 * (la petición HTTP que se convierte en WebSocket).
 *
 * Aquí todavía tenemos acceso a la HttpSession (la del login), así que copiamos
 * el id y el rol para que Pedidos_Socket sepa quién se está conectando.
 */
public class Configurador_Socket extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {

        // Por si quedaron datos de otra conexión, empezamos limpios
        config.getUserProperties().remove(Sesion_Util.ATR_ROL);
        config.getUserProperties().remove(Sesion_Util.ATR_ID);

        Object objeto = request.getHttpSession(); // null si nunca inició sesión
        if (!(objeto instanceof HttpSession)) {
            return;
        }

        try {
            HttpSession sesion = (HttpSession) objeto;
            Object rol = sesion.getAttribute(Sesion_Util.ATR_ROL);
            Object id = sesion.getAttribute(Sesion_Util.ATR_ID);

            // El mapa no acepta null, por eso solo se guardan si existen
            if (rol != null && id != null) {
                config.getUserProperties().put(Sesion_Util.ATR_ROL, rol);
                config.getUserProperties().put(Sesion_Util.ATR_ID, id);
            }
        } catch (IllegalStateException e) {
            // La sesión ya había caducado: se trata como "sin sesión"
        }
    }
}