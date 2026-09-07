package id.gtilabs.recaptchademo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

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
 * Configuration for reCAPTCHA Enterprise (the "new" reCAPTCHA that lives in the
 * Google Cloud console and is verified through the Assessment API, not the old
 * {@code /recaptcha/api/siteverify} endpoint).
 */
@ConfigurationProperties(prefix = "recaptcha")
public class RecaptchaProperties {

    /** Google Cloud project id that owns the key, e.g. {@code annisa-online}. */
    private String projectId;

    /** Public site key rendered in the browser. */
    private String siteKey;

    /** API key used to call the Assessment API. Server-side only. */
    private String apiKey;

    /** Scores below this value are treated as suspicious. Range 0.0 - 1.0. */
    private double scoreThreshold = 0.5d;

    /** Base URL of the reCAPTCHA Enterprise API. Overridable for tests. */
    private String endpoint = "https://recaptchaenterprise.googleapis.com";

    /**
     * True when no API key is configured. The app then simulates assessments so the
     * UI can be explored without Google Cloud credentials.
     */
    public boolean isDemoMode() {
        return !StringUtils.hasText(apiKey);
    }

    /**
     * The API key is sent as an {@code X-Goog-Api-Key} header rather than a {@code ?key=}
     * query parameter, so it cannot leak into proxy or access logs.
     */
    public String assessmentUrl() {
        return "%s/v1/projects/%s/assessments".formatted(endpoint, projectId);
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public double getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
