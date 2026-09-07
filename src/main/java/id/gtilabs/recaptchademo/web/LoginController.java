package id.gtilabs.recaptchademo.web;

import id.gtilabs.recaptchademo.recaptcha.AssessmentResult;
import id.gtilabs.recaptchademo.recaptcha.RecaptchaService;
import id.gtilabs.recaptchademo.web.form.LoginForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created by IntelliJ IDEA.
 * Project : recaptcha-enterprise-demo
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 07/09/26
 * Time: 11.16
 */
@Controller
public class LoginController {

    /** Must match the action passed to grecaptcha.enterprise.execute() in login.html. */
    private static final String ACTION = "LOGIN";

    private final RecaptchaService recaptchaService;

    public LoginController(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @GetMapping("/login")
    public String showForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("action", ACTION);
        return "login";
    }

    @PostMapping("/login")
    public String submit(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                         BindingResult bindingResult,
                         HttpServletRequest request,
                         Model model) {

        model.addAttribute("action", ACTION);

        // Score the request first: a bot that submits garbage should never reach the
        // credential check, and the assessment is interesting even when the form is invalid.
        AssessmentResult assessment = recaptchaService.assess(loginForm.getRecaptchaToken(), ACTION, request);
        model.addAttribute("assessment", assessment);

        if (bindingResult.hasErrors()) {
            return "login";
        }
        if (!assessment.allowed()) {
            model.addAttribute("blocked", true);
            return "login";
        }

        // Real applications would authenticate here. This demo just reports the verdict.
        model.addAttribute("submitted", true);
        return "login";
    }
}
