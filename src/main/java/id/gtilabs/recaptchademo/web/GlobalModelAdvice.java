package id.gtilabs.recaptchademo.web;

import id.gtilabs.recaptchademo.config.RecaptchaProperties;
import id.gtilabs.recaptchademo.recaptcha.RecaptchaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/** Makes the site key and demo flag available to every template. */
@ControllerAdvice
public class GlobalModelAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalModelAdvice.class);

    private final RecaptchaProperties properties;

    public GlobalModelAdvice(RecaptchaProperties properties) {
        this.properties = properties;
    }

    @ModelAttribute
    void commonAttributes(Model model) {
        model.addAttribute("siteKey", properties.getSiteKey());
        model.addAttribute("projectId", properties.getProjectId());
        model.addAttribute("demoMode", properties.isDemoMode());
        model.addAttribute("scoreThreshold", properties.getScoreThreshold());
    }

    @ExceptionHandler(RecaptchaException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ModelAndView handleRecaptchaFailure(RecaptchaException ex) {
        log.error("reCAPTCHA verification failed", ex);
        ModelAndView mav = new ModelAndView("error/recaptcha");
        mav.addObject("siteKey", properties.getSiteKey());
        mav.addObject("projectId", properties.getProjectId());
        mav.addObject("demoMode", properties.isDemoMode());
        mav.addObject("scoreThreshold", properties.getScoreThreshold());
        mav.addObject("errorMessage", ex.getMessage());
        return mav;
    }
}
