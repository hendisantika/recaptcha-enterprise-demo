package id.gtilabs.recaptchademo.recaptcha;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Body of {@code POST /v1/projects/{project}/assessments}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessmentRequest(Event event) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Event(
            String token,
            String siteKey,
            String expectedAction,
            String userIpAddress,
            String userAgent) {
    }

    public static AssessmentRequest of(String token, String siteKey, String expectedAction,
                                       String userIpAddress, String userAgent) {
        return new AssessmentRequest(new Event(token, siteKey, expectedAction, userIpAddress, userAgent));
    }
}
