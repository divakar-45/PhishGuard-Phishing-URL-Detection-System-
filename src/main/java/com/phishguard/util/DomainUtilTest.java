package com.phishguard.util;

public class DomainUtilTest {

    public static void main(String[] args) throws Exception {

        String[] urls = {
                "https://www.google.com",
                "https://mail.google.co.uk",
                "https://sub.example.co.in",
                "https://192.168.1.1/login"
        };

        for (String url : urls) {

            String host = DomainUtil.extractHost(url);
            String domain = DomainUtil.extractRegisteredDomain(host);

            System.out.println("URL: " + url);
            System.out.println("Host: " + host);
            System.out.println("Registered Domain: " + domain);
            System.out.println("-------------------------");
        }
    }
}