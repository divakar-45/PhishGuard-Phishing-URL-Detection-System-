package com.phishguard.detection;

import java.util.List;

public class URLRuleDetectorTest {

    public static void main(String[] args) throws Exception {

        URLRuleDetector detector = new URLRuleDetector();

        String[] urls = {
                "https://google.com",
                "http://192.168.1.1/login",
                "http://secure-login-account.example.com/verify",
                "https://bit.ly/example",
                "https://a.b.c.example.com"
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