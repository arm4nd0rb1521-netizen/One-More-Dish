package util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte Map / List / números / texto a JSON sin librerías externas.
 * Es la misma idea que ya usabas dentro de Pedidos_Domicilio_Servlet,
 * pero en un solo lugar para que la usen los servlets y el socket.
 */
public final class Json_Util {

    private Json_Util() {
    }

    public static String aJson(Object o) {
        if (o == null) return "null";
        if (o instanceof Number || o instanceof Boolean) return o.toString();

        if (o instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean primero = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                if (!primero) sb.append(',');
                primero = false;
                sb.append('"').append(esc(String.valueOf(e.getKey()))).append("\":")
                  .append(aJson(e.getValue()));
            }
            return sb.append('}').toString();
        }

        if (o instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            boolean primero = true;
            for (Object x : (List<?>) o) {
                if (!primero) sb.append(',');
                primero = false;
                sb.append(aJson(x));
            }
            return sb.append(']').toString();
        }

        return "\"" + esc(o.toString()) + "\"";
    }

    /** Escapa comillas, diagonales y caracteres de control para que no rompan el JSON. */
    public static String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /** Atajo: mapa("tipo", "pong", "id", 5) -> {"tipo":"pong","id":5} */
    public static Map<String, Object> mapa(Object... claveValor) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < claveValor.length; i += 2) {
            m.put(String.valueOf(claveValor[i]), claveValor[i + 1]);
        }
        return m;
    }
}
