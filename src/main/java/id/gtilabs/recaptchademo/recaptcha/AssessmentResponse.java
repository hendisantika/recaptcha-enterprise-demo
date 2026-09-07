package id.gtilabs.recaptchademo.recaptcha;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Subset of the Assessment API response that this demo cares about.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AssessmentResponse(
        String name,
        TokenProperties tokenProperties,
        RiskAnalysis riskAnalysis) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TokenProperties(
            boolean valid,
            String invalidReason,
            String hostname,
            String action,
            String createTime) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RiskAnalysis(
            Double score,
            List<String> reasons,
            List<String> extendedVerdictReasons) {
    }
}
