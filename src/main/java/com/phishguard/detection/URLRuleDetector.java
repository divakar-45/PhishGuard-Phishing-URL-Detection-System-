package com.phishguard.detection;

import com.phishguard.util.DomainUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class URLRuleDetector {

    private static final List<String> SUSPICIOUS_KEYWORDS = Arrays.asList(
            "login", "verify", "account", "update",
            "password", "secure", "bank", "signin"
    );

    private static final List<String> URL_SHORTENERS = Arrays.asList(
            "bit.ly", "tinyurl.com", "t.co",
            "is.gd", "cutt.ly", "shorturl.at"
    );

    public List<String> detectIndicators(String url) throws Exception {

        List<String> indicators = new ArrayList<>();

        URI uri = new URI(url);
        String host = DomainUtil.extractHost(url);
        String lowerUrl = url.toLowerCase();

        if (host.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
            indicators.add("IP Address Used Instead of Domain");
        }

        if (url.length() > 100) {
            indicators.add("Excessive URL Length");
        }

        for (String keyword : SUSPICIOUS_KEYWORDS) {
            if (lowerUrl.contains(keyword)) {
                indicators.add("Suspicious Keyword: " + keyword);
                break;
            }
        }

        if (lowerUrl.contains("@") || lowerUrl.contains("%40")
                || lowerUrl.contains("..")) {
            indicators.add("Abnormal URL Symbols");
        }

        if ("http".equalsIgnoreCase(uri.getScheme())) {
            indicators.add("Insecure HTTP Protocol");
        }

        for (String shortener : URL_SHORTENERS) {
            if (host.equals(shortener) || host.endsWith("." + shortener)) {
                indicators.add("URL Shortening Service Detected");
                break;
            }
        }

        String registeredDomain =
                DomainUtil.extractRegisteredDomain(host);

        if (!host.equals(registeredDomain)
                && host.endsWith("." + registeredDomain)) {

            String subdomainPart = host.substring(
                    0, host.length() - registeredDomain.length() - 1
            );

            if (subdomainPart.split("\\.").length > 2) {
                indicators.add("Multiple Subdomains Detected");
            }
        }

        return indicators;
    }
}
