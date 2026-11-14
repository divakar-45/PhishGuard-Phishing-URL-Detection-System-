package com.phishguard.detection;

import com.phishguard.database.TrustedDomainDAO;
import com.phishguard.util.DomainUtil;

import java.util.List;

public class DetectionEngine {

    private TrustedDomainDAO trustedDomainDAO;
    private URLRuleDetector ruleDetector;

    public DetectionEngine() {
        trustedDomainDAO = new TrustedDomainDAO();
        ruleDetector = new URLRuleDetector();
    }

    public DetectionResult analyzeURL(String url) throws Exception {

        String host = DomainUtil.extractHost(url);

        String registeredDomain =
                DomainUtil.extractRegisteredDomain(host);

        boolean trusted = trustedDomainDAO.isTrustedDomain(
                registeredDomain
        );

        List<String> indicators =
                ruleDetector.detectIndicators(url);

        return new DetectionResult(
                url,
                host,
                registeredDomain,
                trusted,
                indicators
        );
    }
}