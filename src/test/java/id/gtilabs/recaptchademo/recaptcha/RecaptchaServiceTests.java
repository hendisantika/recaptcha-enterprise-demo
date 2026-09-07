package id.gtilabs.recaptchademo.recaptcha;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.gtilabs.recaptchademo.config.RecaptchaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RecaptchaServiceTests {

    private static final String URL =
            "https://recaptchaenterprise.example/v1/projects/annisa-online/assessments";

    private RecaptchaProperties properties;
    private MockRestServiceServer server;
    private RecaptchaService service;

    @BeforeEach
    void setUp() {
        properties = new RecaptchaProperties();
        properties.setProjectId("annisa-online");
        properties.setSiteKey("test-site-key");
        properties.setApiKey("test-api-key");
        properties.setScoreThreshold(0.5d);
        properties.setEndpoint("https://recaptchaenterprise.example");

        RestClient.Builder builder = RestClient.builder();
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.service = new RecaptchaService(builder.build(), properties, new ObjectMapper());
    }

    @Test
    void allowsHighScoreWithMatchingAction() {
        server.expect(requestTo(URL))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("X-Goog-Api-Key", "test-api-key"))
                .andExpect(jsonPath("$.event.token").value("token-123"))
                .andExpect(jsonPath("$.event.siteKey").value("test-site-key"))
                .andExpect(jsonPath("$.event.expectedAction").value("LOGIN"))
                .andRespond(withSuccess("""
                        {
                          "name": "projects/annisa-online/assessments/abc",
                          "tokenProperties": {"valid": true, "hostname": "localhost", "action": "LOGIN"},
                          "riskAnalysis": {"score": 0.9, "reasons": []}
                        }""", MediaType.APPLICATION_JSON));

        AssessmentResult result = service.assess("token-123", "LOGIN", null);

        assertThat(result.tokenValid()).isTrue();
        assertThat(result.actionMatched()).isTrue();
        assertThat(result.score()).isEqualTo(0.9d);
        assertThat(result.allowed()).isTrue();
        assertThat(result.riskBand()).isEqualTo("LOW");
        server.verify();
    }

    @Test
    void blocksScoreBelowThreshold() {
        server.expect(requestTo(URL)).andRespond(withSuccess("""
                {
                  "tokenProperties": {"valid": true, "hostname": "localhost", "action": "LOGIN"},
                  "riskAnalysis": {"score": 0.2, "reasons": ["AUTOMATION"]}
                }""", MediaType.APPLICATION_JSON));

        AssessmentResult result = service.assess("token-123", "LOGIN", null);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly("AUTOMATION");
        assertThat(result.riskBand()).isEqualTo("HIGH");
        server.verify();
    }

    @Test
    void blocksWhenActionDoesNotMatch() {
        server.expect(requestTo(URL)).andRespond(withSuccess("""
                {
                  "tokenProperties": {"valid": true, "hostname": "localhost", "action": "SIGNUP"},
                  "riskAnalysis": {"score": 0.9, "reasons": []}
                }""", MediaType.APPLICATION_JSON));

        AssessmentResult result = service.assess("token-123", "LOGIN", null);

        assertThat(result.actionMatched()).isFalse();
        assertThat(result.allowed()).isFalse();
        server.verify();
    }

    @Test
    void reportsInvalidToken() {
        server.expect(requestTo(URL)).andRespond(withSuccess("""
                {
                  "tokenProperties": {"valid": false, "invalidReason": "EXPIRED"}
                }""", MediaType.APPLICATION_JSON));

        AssessmentResult result = service.assess("token-123", "LOGIN", null);

        assertThat(result.tokenValid()).isFalse();
        assertThat(result.invalidReason()).isEqualTo("EXPIRED");
        assertThat(result.allowed()).isFalse();
        server.verify();
    }

    @Test
    void rejectsMissingTokenWithoutCallingTheApi() {
        AssessmentResult result = service.assess("  ", "LOGIN", null);

        assertThat(result.tokenValid()).isFalse();
        assertThat(result.invalidReason()).isEqualTo("MISSING_TOKEN");
        server.verify(); // no request was expected, and none was made
    }

    @Test
    void simulatesAssessmentWhenNoApiKeyIsConfigured() {
        properties.setApiKey("");

        AssessmentResult result = service.assess("token-123", "CONTACT", null);

        assertThat(result.demo()).isTrue();
        assertThat(result.tokenValid()).isTrue();
        assertThat(result.action()).isEqualTo("CONTACT");
        assertThat(result.score()).isBetween(0.0d, 1.0d);
        assertThat(result.rawJson()).contains("SIMULATED");
    }
}
