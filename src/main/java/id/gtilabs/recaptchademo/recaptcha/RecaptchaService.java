package id.gtilabs.recaptchademo.recaptcha;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import id.gtilabs.recaptchademo.config.RecaptchaProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Created by IntelliJ IDEA.
 * Project : recaptcha-enterprise-demo
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 07/09/26
 * Time: 11.16
 */

/**
 * Calls the reCAPTCHA Enterprise Assessment API.
 *
 * <p>The old reCAPTCHA v2/v3 flow posted the token to
 * {@code https://www.google.com/recaptcha/api/siteverify} with a shared secret and got back
 * {@code {"success": true}}. The new (Enterprise) flow posts an <em>assessment</em> to
 * {@code https://recaptchaenterprise.googleapis.com/v1/projects/{project}/assessments} and gets
 * back a risk score plus reason codes, which the application then turns into its own policy.
 */
@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);

    private final RestClient restClient;
    private final RecaptchaProperties properties;
    private final ObjectMapper objectMapper;

    public RecaptchaService(RestClient recaptchaRestClient, RecaptchaProperties properties,
                            ObjectMapper objectMapper) {
        this.restClient = recaptchaRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates an assessment for a token produced by {@code grecaptcha.enterprise.execute()}.
     *
     * @param token          the token from the browser (single use, expires after two minutes)
     * @param expectedAction the action the server expects, e.g. {@code LOGIN}
     * @param request        used only to pass the caller IP and user agent to reCAPTCHA
     */
    public AssessmentResult assess(String token, String expectedAction, HttpServletRequest request) {
        if (properties.isDemoMode()) {
            // Checked before the token check so the demo works with no network and no key.
            log.warn("recaptcha.api-key is not set - returning a SIMULATED assessment for action {}", expectedAction);
            return simulate(expectedAction);
        }
        if (!StringUtils.hasText(token)) {
            return rejected("MISSING_TOKEN", expectedAction);
        }

        AssessmentRequest body = AssessmentRequest.of(
                token,
                properties.getSiteKey(),
                expectedAction,
                clientIp(request),
                request != null ? request.getHeader("User-Agent") : null);

        String rawJson;
        try {
            rawJson = restClient.post()
                    .uri(URI.create(properties.assessmentUrl()))
                    .header("X-Goog-Api-Key", properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        }
        catch (RestClientException ex) {
            throw new RecaptchaException("Assessment API call failed: " + ex.getMessage(), ex);
        }

        if (!StringUtils.hasText(rawJson)) {
            throw new RecaptchaException("Assessment API returned an empty body");
        }

        AssessmentResponse response;
        try {
            response = objectMapper.readValue(rawJson, AssessmentResponse.class);
        }
        catch (JacksonException ex) {
            throw new RecaptchaException("Could not parse the assessment response", ex);
        }

        return toResult(response, expectedAction, prettyPrint(rawJson));
    }

    private AssessmentResult toResult(AssessmentResponse response, String expectedAction, String rawJson) {
        AssessmentResponse.TokenProperties tokenProperties = response.tokenProperties();
        if (tokenProperties == null || !tokenProperties.valid()) {
            String reason = tokenProperties != null ? tokenProperties.invalidReason() : "MISSING_TOKEN_PROPERTIES";
            log.info("reCAPTCHA token rejected: {}", reason);
            return new AssessmentResult(false, reason, 0.0d, expectedAction, false,
                    List.of(), tokenProperties != null ? tokenProperties.hostname() : null,
                    false, properties.getScoreThreshold(), false, rawJson);
        }

        // The action is signed into the token by the browser. Comparing it to the action the
        // server expects is what stops a token minted on a harmless page being replayed here.
        boolean actionMatched = equalsIgnoreCase(tokenProperties.action(), expectedAction);
        double score = response.riskAnalysis() != null && response.riskAnalysis().score() != null
                ? response.riskAnalysis().score()
                : 0.0d;
        List<String> reasons = response.riskAnalysis() != null && response.riskAnalysis().reasons() != null
                ? response.riskAnalysis().reasons()
                : List.of();
        boolean allowed = actionMatched && score >= properties.getScoreThreshold();

        log.debug("assessment action={} expected={} score={} allowed={}",
                tokenProperties.action(), expectedAction, score, allowed);

        return new AssessmentResult(true, null, score, tokenProperties.action(), actionMatched,
                reasons, tokenProperties.hostname(), allowed, properties.getScoreThreshold(), false, rawJson);
    }

    private AssessmentResult rejected(String invalidReason, String expectedAction) {
        return new AssessmentResult(false, invalidReason, 0.0d, expectedAction, false, List.of(),
                null, false, properties.getScoreThreshold(), properties.isDemoMode(),
                "{\n  \"error\": \"no token was submitted\"\n}");
    }

    /** Used when no API key is configured, so the UI can still be demonstrated offline. */
    private AssessmentResult simulate(String expectedAction) {
        double score = Math.round(ThreadLocalRandom.current().nextDouble(0.1d, 1.0d) * 10d) / 10d;
        List<String> reasons = score < properties.getScoreThreshold()
                ? List.of("LOW_CONFIDENCE_SCORE")
                : List.of();
        String rawJson = """
                {
                  "name": "projects/%s/assessments/SIMULATED",
                  "tokenProperties": {
                    "valid": true,
                    "hostname": "localhost",
                    "action": "%s"
                  },
                  "riskAnalysis": {
                    "score": %s,
                    "reasons": %s
                  },
                  "_note": "SIMULATED - set RECAPTCHA_API_KEY to call the real Assessment API"
                }""".formatted(properties.getProjectId(), expectedAction, score, toJsonArray(reasons));
        return new AssessmentResult(true, null, score, expectedAction, true, reasons, "localhost",
                score >= properties.getScoreThreshold(), properties.getScoreThreshold(), true, rawJson);
    }

    private static String toJsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + value + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String prettyPrint(String rawJson) {
        try {
            Object tree = objectMapper.readValue(rawJson, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
        }
        catch (JacksonException ex) {
            return rawJson;
        }
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null
                && Objects.equals(a.toLowerCase(Locale.ROOT), b.toLowerCase(Locale.ROOT));
    }

    /**
     * X-Forwarded-For is deliberately NOT parsed here: any client can forge it and poison the
     * IP-reputation signal. Set {@code server.forward-headers-strategy} so that Spring's
     * ForwardedHeaderFilter resolves the real address, only where a proxy is actually trusted.
     */
    private static String clientIp(HttpServletRequest request) {
        return request != null ? request.getRemoteAddr() : null;
    }
}
