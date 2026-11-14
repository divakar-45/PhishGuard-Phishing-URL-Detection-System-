package com.phishguard.detection;

import java.util.List;

public class URLRuleDetectorTest {

    public static void main(String[] args) throws Exception {

        URLRuleDetector detector = new URLRuleDetector();

        String[] urls = {
                "https://example.com/" + "a".repeat(70),
                "https://example.com/path--name",
                "https://example.com/path//login",
                "https://example.com/search?q=%20",
                "http://[2001:db8::1]/login"
        };

        for (String url : urls) {

            System.out.println("URL: " + url);

            List<String> indicators =
                    detector.detectIndicators(url);

            if (indicators.isEmpty()) {
                System.out.println("No suspicious indicators detected.");
            } else {
                for (String indicator : indicators) {
                    System.out.println("- " + indicator);
                }
            }

            System.out.println("-------------------------");
        }
    }
}