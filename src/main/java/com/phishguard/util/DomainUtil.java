package com.phishguard.util;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DomainUtil {

    private static final Set<String> MULTI_PART_SUFFIXES = new HashSet<>(
            Arrays.asList(
                    "co.uk", "org.uk", "ac.uk",
                    "com.au", "net.au", "org.au",
                    "co.in", "firm.in", "net.in",
                    "org.in", "gen.in", "ind.in",
                    "co.jp", "co.nz", "com.br"
            )
    );

    public static String extractHost(String url) throws URISyntaxException {

        URI uri = new URI(url);
        String host = uri.getHost();

        if (host == null || host.isBlank()) {
            throw new URISyntaxException(url, "Host not found");
        }

        host = host.toLowerCase(Locale.ROOT);
        host = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;

        if (host.startsWith("[") && host.endsWith("]")) {
            return host;
        }

        return IDN.toASCII(host);
    }

    public static String extractRegisteredDomain(String host) {

        if (host.startsWith("[") || host.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
            return host;
        }

        String[] parts = host.split("\\.");

        if (parts.length < 2) {
            return host;
        }

        String lastTwo = parts[parts.length - 2] + "." + parts[parts.length - 1];

        if (MULTI_PART_SUFFIXES.contains(lastTwo) && parts.length >= 3) {
            return parts[parts.length - 3] + "." + lastTwo;
        }

        return lastTwo;
    }
}