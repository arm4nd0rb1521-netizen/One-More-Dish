package util;

import org.mindrot.jbcrypt.BCrypt;

public class Password_Service {

    public static String generarHash(String contraseña) {
        return BCrypt.hashpw(
            contraseña,
            BCrypt.gensalt(12)
        );
    }

    public static boolean verificar(
            String contraseña,
            String hashGuardado) {

        if (contraseña == null || hashGuardado == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(contraseña, hashGuardado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}