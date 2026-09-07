package id.gtilabs.recaptchademo.recaptcha;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
