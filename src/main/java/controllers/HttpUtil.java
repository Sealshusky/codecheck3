package controllers;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {
    public static String absolutePath(UriInfo uriInfo,  HttpHeaders headers) {
        URI uri = uriInfo.getRequestUri();
        if (headers.getRequestHeader("X-Forwarded-Proto").contains("https")) { // TODO: This was needed with Play. Ist it still needed?
            try {
                uri = new URI("https", uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return uri.toString();
    }

    public static String prefix(UriInfo uriInfo,  HttpHeaders headers) {
        URI uri = uriInfo.getBaseUri();
        boolean secure = uri.getScheme().equals("https") ||
                headers.getRequestHeader("X-Forwarded-Proto").contains("https"); // TODO: This was needed with Play. Ist it still needed?
        String host = uri.getHost();
        String path = uriInfo.getPath();

        String prefix;
        if (host.equals("localhost")) {
            prefix = "../";
            long countSlash = path.chars().filter(ch -> ch == '/').count() - 1;
            for (long i = 0; i < countSlash; ++i) {
                prefix += "../";
            }
            prefix = prefix.substring(0, prefix.length() - 1);
        } else {
            prefix = (secure ? "https://" : "http://") + host;
        }
        return prefix;
    }

    public static NewCookie buildCookie(String name, String value) {
        return new NewCookie.Builder(name)
                .value(value)
                .path("/")
                .maxAge(60 * 60 * 24 * 180) // 180 days TODO Even for jwt???
                .secure(true)
                .sameSite(NewCookie.SameSite.NONE)
                .httpOnly(true)
                .build();
    }

    // TODO: Fix so that it works for repeated keys
    static Map<String, String[]> paramsMap(MultivaluedMap<String, String> formParams) {
        Map<String, String[]> postParams = new HashMap<>();
        for (var entry : formParams.entrySet()) {
            postParams.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }
        return postParams;
    }
}
