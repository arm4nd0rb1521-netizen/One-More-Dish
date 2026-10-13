
package util;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

public class Correo_Service {

    public static void enviarCodigo(
            String destinatario,
            String codigo) throws ResendException {

        String apiKey = System.getenv("RESEND_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "No se configuró RESEND_API_KEY"
            );
        }

        Resend resend = new Resend(apiKey);

        CreateEmailOptions correo =
            CreateEmailOptions.builder()
                .from("One More Dish <onboarding@resend.dev>")
                .to(destinatario)
                .subject("Verifica tu correo")
                .html(
                    "<h2>One More Dish</h2>"
                    + "<p>Tu código de verificación es:</p>"
                    + "<h1>" + codigo + "</h1>"
                    + "<p>Este código vence en 10 minutos.</p>"
                )
                .build();

        resend.emails().send(correo);
    }
}
