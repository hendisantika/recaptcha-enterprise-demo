package id.gtilabs.recaptchademo;

import id.gtilabs.recaptchademo.config.RecaptchaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RecaptchaProperties.class)
public class RecaptchaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecaptchaDemoApplication.class, args);
    }
}
