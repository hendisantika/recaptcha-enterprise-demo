package id.gtilabs.recaptchademo.recaptcha;

public class RecaptchaException extends RuntimeException {

    public RecaptchaException(String message) {
        super(message);
    }

    public RecaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}
