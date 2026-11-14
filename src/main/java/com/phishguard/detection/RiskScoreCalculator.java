
package com.phishguard.detection;

import java.util.List;

public class RiskScoreCalculator {

    public int calculateScore(List<String> indicators) {

        int score = 0;

        for (String indicator : indicators) {

            if (indicator.startsWith("IPv4 Address")
                    || indicator.startsWith("IPv6 Address")) {
                score += 25;
            }

            else if (indicator.equals("Excessive URL Length")) {
                score += 10;
            }

            else if (indicator.startsWith("Suspicious Keyword:")) {
                score += 10;
            }

            else if (indicator.equals("Multiple Subdomains Detected")) {
                score += 15;
            }

            else if (indicator.equals("URL Shortening Service Detected")) {
                score += 15;
            }

            else if (indicator.equals("Insecure HTTP Protocol")) {
                score += 10;
            }

            else if (indicator.equals("Abnormal URL Symbols")) {
                score += 15;
            }
        }

        return Math.min(score, 100);
    }

    public String classifyRisk(int score) {

        if (score <= 30) {
            return "SAFE";
        }

        else if (score <= 65) {
            return "SUSPICIOUS";
        }

        return "PHISHING";
    }
}
