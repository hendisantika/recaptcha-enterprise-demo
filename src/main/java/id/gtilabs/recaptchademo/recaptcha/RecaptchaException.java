package id.gtilabs.recaptchademo.recaptcha;

/**
 * Created by IntelliJ IDEA.
 * Project : recaptcha-enterprise-demo
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 07/09/26
 * Time: 11.16
 */
public class RecaptchaException extends RuntimeException {

    public RecaptchaException(String message) {
        super(message);
    }

    public RecaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}
