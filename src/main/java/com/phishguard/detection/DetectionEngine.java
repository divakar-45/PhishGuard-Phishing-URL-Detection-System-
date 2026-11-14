
package com.phishguard.detection;

import com.phishguard.database.TrustedDomainDAO;
import com.phishguard.util.DomainUtil;

import java.util.ArrayList;
import java.util.List;

public class DetectionEngine {

    private TrustedDomainDAO trustedDomainDAO;
    private URLRuleDetector ruleDetector;
    private RiskScoreCalculator riskScoreCalculator;

    public DetectionEngine() {

        trustedDomainDAO = new TrustedDomainDAO();
        ruleDetector = new URLRuleDetector();
        riskScoreCalculator = new RiskScoreCalculator();
    }

    public DetectionResult analyzeURL(String url) throws Exception {

        String host = DomainUtil.extractHost(url);

        String registeredDomain =
                DomainUtil.extractRegisteredDomain(host);

        boolean trusted = trustedDomainDAO.isTrustedDomain(
                registeredDomain
        );

        if (trusted) {

            return new DetectionResult(
                    url,
                    host,
                    registeredDomain,
                    true,
                    new ArrayList<>(),
                    0,
                    "SAFE"
            );
        }

        List<String> indicators =
                ruleDetector.detectIndicators(url);

        int riskScore =
                riskScoreCalculator.calculateScore(indicators);

        String classification =
                riskScoreCalculator.classifyRisk(riskScore);

        return new DetectionResult(
                url,
                host,
                registeredDomain,
                false,
                indicators,
                riskScore,
                classification
        );
    }
}
