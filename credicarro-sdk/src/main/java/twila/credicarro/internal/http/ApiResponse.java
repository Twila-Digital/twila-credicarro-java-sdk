package twila.credicarro.internal.http;

import okhttp3.Headers;

public final class ApiResponse {

    private final int statusCode;
    private final String body;
    private final Headers headers;

    public ApiResponse(int statusCode, String body, Headers headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }

    public Headers headers() {
        return headers;
    }
}
