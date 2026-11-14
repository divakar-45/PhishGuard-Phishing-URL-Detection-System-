
package com.phishguard.detection;

import com.phishguard.util.DomainUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class URLRuleDetector {

    private static final List<String> SUSPICIOUS_KEYWORDS = Arrays.asList(
            "login", "verification", "security", "update", "banking"
    );

    private static final List<String> URL_SHORTENERS = Arrays.asList(
            "bit.ly", "tinyurl.com", "t.co",
            "is.gd", "cutt.ly", "shorturl.at", "ow.ly"
    );

    public List<String> detectIndicators(String url) throws Exception {

        List<String> indicators = new ArrayList<>();

        URI uri = new URI(url);

        String host = DomainUtil.extractHost(url).toLowerCase();
        String lowerUrl = url.toLowerCase();

        String path = uri.getRawPath() == null
                ? "" : uri.getRawPath().toLowerCase();

        String query = uri.getRawQuery() == null
                ? "" : uri.getRawQuery().toLowerCase();

        String keywordText = host + " " + path + " " + query;

        if (host.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
            indicators.add("IPv4 Address Used Instead of Domain");
        }

        if (host.contains(":")) {
            indicators.add("IPv6 Address Used Instead of Domain");
        }

        if (url.length() > 75) {
            indicators.add("Excessive URL Length");
        }

        for (String keyword : SUSPICIOUS_KEYWORDS) {
            if (keywordText.contains(keyword)) {
                indicators.add("Suspicious Keyword: " + keyword);
                break;
            }
        }

        if (lowerUrl.contains("@")
                || lowerUrl.contains("--")
                || path.contains("//")
                || lowerUrl.contains("%20")) {

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

        if (registeredDomain != null
                && !host.equals(registeredDomain)
                && host.endsWith("." + registeredDomain)) {

            String subdomainPart = host.substring(
                    0, host.length() - registeredDomain.length() - 1
            );

            if (subdomainPart.split("\\.").length >= 3) {
                indicators.add("Multiple Subdomains Detected");
            }
        }

        return indicators;
    }
}
