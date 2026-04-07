package testsupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class StubHttps {
    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);
    private static volatile Responder responder;

    private StubHttps() {
    }

    public static void install() {
        if (!INSTALLED.compareAndSet(false, true)) {
            return;
        }
        try {
            URL.setURLStreamHandlerFactory(new StubFactory());
        } catch (Error ignored) {
            // Another factory was already set.
        }
    }

    public static void setResponder(Responder responder) {
        StubHttps.responder = responder;
    }

    public static void reset() {
        StubHttps.responder = null;
    }

    public interface Responder {
        StubResponse respond(StubRequest request);
    }

    public static final class StubRequest {
        private final URL url;
        private final String method;
        private final byte[] body;
        private final Map<String, String> headers;

        private StubRequest(URL url, String method, byte[] body, Map<String, String> headers) {
            this.url = url;
            this.method = method;
            this.body = body;
            this.headers = headers;
        }

        public URL getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public byte[] getBody() {
            return body;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }
    }

    public static final class StubResponse {
        private final int statusCode;
        private final String body;

        public StubResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }

    private static final class StubFactory implements URLStreamHandlerFactory {
        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (!"https".equalsIgnoreCase(protocol)) {
                return null;
            }
            return new StubHandler();
        }
    }

    private static final class StubHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL url) {
            return new StubConnection(url);
        }
    }

    private static final class StubConnection extends HttpURLConnection {
        private final ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
        private final Map<String, String> headers = new HashMap<>();
        private StubResponse response;

        protected StubConnection(URL url) {
            super(url);
        }

        @Override
        public void setRequestProperty(String key, String value) {
            headers.put(key, value);
        }

        @Override
        public OutputStream getOutputStream() {
            return requestBody;
        }

        @Override
        public int getResponseCode() throws IOException {
            ensureResponse();
            return response.getStatusCode();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            ensureResponse();
            if (response.getStatusCode() >= 400) {
                throw new IOException("HTTP error: " + response.getStatusCode());
            }
            if (response.getBody() == null) {
                return null;
            }
            return new ByteArrayInputStream(response.getBody().getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public InputStream getErrorStream() {
            ensureResponse();
            if (response.getStatusCode() < 400) {
                return null;
            }
            if (response.getBody() == null) {
                return null;
            }
            return new ByteArrayInputStream(response.getBody().getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void disconnect() {
            // no-op
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() {
            // no-op
        }

        private void ensureResponse() {
            if (response != null) {
                return;
            }
            Responder responder = StubHttps.responder;
            StubRequest request = new StubRequest(url, getRequestMethod(), requestBody.toByteArray(),
                    Collections.unmodifiableMap(headers));
            if (responder == null) {
                response = new StubResponse(500, "");
            } else {
                response = responder.respond(request);
            }
        }
    }
}

