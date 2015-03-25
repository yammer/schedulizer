package com.yammer.schedulizer.auth;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.yammer.schedulizer.utils.CoreUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/* TODO: Support for base64 encoded contents */
public class AuthorizationHeader {

    private static final String MALFORMED_HEADER_MESSAGE =
            "Malformed header, check http://tools.ietf.org/html/draft-ietf-httpbis-p7-auth-19#section-4.4";

    public static AuthorizationHeader decode(String header) {
        checkNotNull(header);
        checkArgument(header.trim().contains(" "), MALFORMED_HEADER_MESSAGE);

        String[] parts = CoreUtils.splitOnFirstOccurrenceAndTrim(header.trim(), " ");
        String scheme = parts[0];
        String rest = parts[1];

        try {
            CharMatcher quotes = CharMatcher.is('"');
            Map<String, String> params = Splitter
                    .on(",")
                    .trimResults()
                    .omitEmptyStrings()
                    .splitToList(rest)                                          // List of "key = value"
                    .stream()
                    .map(p -> CoreUtils.splitOnFirstOccurrenceAndTrim(p, "="))  // List of {"key", "value"}
                    .collect(Collectors.toMap(
                            p -> p[0],
                            p -> quotes.trimFrom(p[1])));                       // Map "key" => "value"

            return new AuthorizationHeader(scheme, params);

        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(MALFORMED_HEADER_MESSAGE, e);
        }
    }

    private String scheme;
    private Map<String, String> parameters;

    private AuthorizationHeader(String scheme, Map<String, String> parameters) {
        this.scheme = scheme;
        this.parameters = new HashMap<>(parameters);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public boolean hasParameter(String name) {
        return parameters.containsKey(name);
    }

    /**
     * Returns scheme always in lowercase since scheme is case insensitive according to RFC
     * @return scheme in lowercase
     */
    public String getScheme() {
        return scheme.toLowerCase();
    }

    public boolean isScheme(String scheme) {
        return this.scheme.toLowerCase().equals(scheme.toLowerCase());
    }

    public Map<String, String> getParameters() {
        return ImmutableMap.copyOf(parameters);
    }
}
