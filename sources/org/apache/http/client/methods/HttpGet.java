package org.apache.http.client.methods;

import java.net.URI;

public class HttpGet extends HttpRequestBase {
    public static final String METHOD_NAME = "GET";

    public HttpGet() {
    }

    public HttpGet(URI uri) {
        setURI(uri);
    }

    public HttpGet(String str) {
        setURI(URI.create(str));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}
