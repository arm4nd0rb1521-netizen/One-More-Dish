package util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class Codigo_Service {

    private static final SecureRandom random =
            new SecureRandom();

    public static String generarCodigo() {
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    public static String generarHash(String codigo)
            throws Exception {

        MessageDigest md =
                MessageDigest.getInstance("SHA-256");

        byte[] resultado = md.digest(
                codigo.getBytes(StandardCharsets.UTF_8)
        );

        return HexFormat.of().formatHex(resultado);
    }
}
