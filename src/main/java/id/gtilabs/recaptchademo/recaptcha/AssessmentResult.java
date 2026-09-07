package id.gtilabs.recaptchademo.recaptcha;

import java.util.List;
import java.util.Locale;

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
 * What the controllers actually need: was the token usable, what did it score,
 * and does that clear our threshold.
 *
 * @param tokenValid   reCAPTCHA accepted the token (not expired, not reused, right site key)
 * @param invalidReason why the token was rejected, when {@code tokenValid} is false
 * @param score        0.0 (very likely a bot) to 1.0 (very likely a human)
 * @param action       the action name that was baked into the token by the browser
 * @param actionMatched the token's action equals the action the server expected
 * @param reasons      risk-analysis reason codes, e.g. AUTOMATION, LOW_CONFIDENCE_SCORE
 * @param hostname     the site that produced the token
 * @param allowed      final verdict: token valid, action matched, score >= threshold
 * @param threshold    the threshold the score was compared against
 * @param demo         result was simulated because no API key is configured
 * @param rawJson      pretty-printed API response, shown in the UI for teaching purposes
 */
public record AssessmentResult(
        boolean tokenValid,
        String invalidReason,
        double score,
        String action,
        boolean actionMatched,
        List<String> reasons,
        String hostname,
        boolean allowed,
        double threshold,
        boolean demo,
        String rawJson) {

    /** Coarse label used for styling: LOW / MEDIUM / HIGH risk. */
    public String riskBand() {
        if (!tokenValid || !actionMatched) {
            return "INVALID";
        }
        if (score >= 0.7d) {
            return "LOW";
        }
        return score >= 0.3d ? "MEDIUM" : "HIGH";
    }

    public String scoreLabel() {
        return String.format(Locale.ROOT, "%.1f", score);
    }

    public int scorePercent() {
        return (int) Math.round(score * 100);
    }
}
