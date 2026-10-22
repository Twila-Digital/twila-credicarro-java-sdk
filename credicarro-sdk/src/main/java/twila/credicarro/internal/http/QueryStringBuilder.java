package twila.credicarro.internal.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public final class QueryStringBuilder {

    private final List<String> parameters = new ArrayList<>();

    public QueryStringBuilder add(String name, String value) {
        if (value != null)
            parameters.add(encode(name) + "=" + encode(value));

        return this;
    }

    public QueryStringBuilder add(String name, Integer value) {
        return add(name, value != null ? value.toString() : null);
    }

    public String build(String path) {
        if (parameters.isEmpty())
            return path;

        return path + "?" + String.join("&", parameters);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 não suportado pela JVM.", ex);
        }
    }
}
