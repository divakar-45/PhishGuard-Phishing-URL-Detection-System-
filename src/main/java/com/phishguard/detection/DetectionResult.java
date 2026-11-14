package com.phishguard.detection;

import java.util.List;

public class DetectionResult {

    private String submittedUrl;
    private String host;
    private String registeredDomain;
    private boolean trustedDomain;
    private List<String> indicators;

    public DetectionResult(String submittedUrl, String host,
            String registeredDomain, boolean trustedDomain,
            List<String> indicators) {

        this.submittedUrl = submittedUrl;
        this.host = host;
        this.registeredDomain = registeredDomain;
        this.trustedDomain = trustedDomain;
        this.indicators = indicators;
    }

    public String getSubmittedUrl() {
        return submittedUrl;
    }

    public String getHost() {
        return host;
    }

    public String getRegisteredDomain() {
        return registeredDomain;
    }

    public boolean isTrustedDomain() {
        return trustedDomain;
    }

    public List<String> getIndicators() {
        return indicators;
    }
}