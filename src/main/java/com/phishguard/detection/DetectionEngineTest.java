
package com.phishguard.detection;

public class DetectionEngineTest {

    public static void main(String[] args) {

        DetectionEngine engine = new DetectionEngine();

        String[] urls = {
                "https://google.com",
                "http://192.168.1.1/login",
                "https://bit.ly/example",
                "https://unknown-example.com/login"
        };

        for (String url : urls) {

            try {

                DetectionResult result = engine.analyzeURL(url);

                System.out.println("Submitted URL: "
                        + result.getSubmittedUrl());

                System.out.println("Host: "
                        + result.getHost());

                System.out.println("Registered Domain: "
                        + result.getRegisteredDomain());

                System.out.println("Trusted Domain: "
                        + (result.isTrustedDomain() ? "YES" : "NO"));

                System.out.println("Risk Score: "
                        + result.getRiskScore());

                System.out.println("Classification: "
                        + result.getClassification());

                System.out.println("Detected Indicators:");

                if (result.getIndicators().isEmpty()) {
                    System.out.println("No suspicious indicators detected.");
                } else {
                    for (String indicator : result.getIndicators()) {
                        System.out.println("- " + indicator);
                    }
                }

                System.out.println("--------------------------------");

            } catch (Exception e) {
                System.out.println("Analysis Error: " + e.getMessage());
            }
        }
    }
}
