package id.gtilabs.recaptchademo.web;

import id.gtilabs.recaptchademo.recaptcha.AssessmentResult;
import id.gtilabs.recaptchademo.recaptcha.RecaptchaService;
import id.gtilabs.recaptchademo.web.form.ContactForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ContactController {

    /** Must match the action passed to grecaptcha.enterprise.execute() in contact.html. */
    private static final String ACTION = "CONTACT";

    private final RecaptchaService recaptchaService;

    public ContactController(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @GetMapping("/contact")
    public String showForm(Model model) {
        model.addAttribute("contactForm", new ContactForm());
        model.addAttribute("action", ACTION);
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("contactForm") ContactForm contactForm,
                         BindingResult bindingResult,
                         HttpServletRequest request,
                         Model model) {

        model.addAttribute("action", ACTION);

        AssessmentResult assessment = recaptchaService.assess(contactForm.getRecaptchaToken(), ACTION, request);
        model.addAttribute("assessment", assessment);

        if (bindingResult.hasErrors()) {
            return "contact";
        }
        if (!assessment.allowed()) {
            model.addAttribute("blocked", true);
            return "contact";
        }

        model.addAttribute("submitted", true);
        return "contact";
    }
}
