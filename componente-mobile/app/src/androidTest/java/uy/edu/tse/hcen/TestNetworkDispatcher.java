package uy.edu.tse.hcen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Installs a URLStreamHandlerFactory that intercepts all HTTP/HTTPS requests and serves
 * canned responses defined from instrumentation tests. This avoids real network traffic.
 */
final class TestNetworkDispatcher {

    private static final Map<String, Queue<StubResponse>> RESPONSES = new ConcurrentHashMap<>();
    private static final Map<String, String> LAST_REQUEST_BODIES = new ConcurrentHashMap<>();
    private static volatile boolean installed = false;

    private TestNetworkDispatcher() {}

    static void install() {
        if (installed) {
            return;
        }
        synchronized (TestNetworkDispatcher.class) {
            if (installed) return;
            try {
                URL.setURLStreamHandlerFactory(new StubHandlerFactory());
            } catch (Error ignored) {
                // Factory already installed by another test run; ignore.
            }
            installed = true;
        }
    }

    static void clear() {
        RESPONSES.clear();
        LAST_REQUEST_BODIES.clear();
    }

    static void enqueueResponse(String url, int code, byte[] body) {
        RESPONSES.computeIfAbsent(url, key -> new ConcurrentLinkedQueue<>()).add(new StubResponse(code, body));
    }

    static void enqueueResponse(String url, int code, String body) {
        enqueueResponse(url, code, body.getBytes(StandardCharsets.UTF_8));
    }

    static String getLastRequestBody(String url) {
        return LAST_REQUEST_BODIES.get(url);
    }

    private static class StubHandlerFactory implements URLStreamHandlerFactory {
        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol)) {
                return new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL url) {
                        Queue<StubResponse> queue = RESPONSES.get(url.toString());
                        StubResponse response = (queue != null && !queue.isEmpty())
                                ? queue.poll()
                                : new StubResponse(HttpURLConnection.HTTP_OK, new byte[0]);
                        return new StubHttpURLConnection(url, response);
                    }
                };
            }
            return null;
        }
    }

    private static class StubHttpURLConnection extends HttpURLConnection {

        private final StubResponse response;
        protected StubHttpURLConnection(URL url, StubResponse response) {
            super(url);
            this.response = response;
        }

        @Override
        public void disconnect() { }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() { }

        @Override
        public InputStream getInputStream() {
            if (response.code >= 200 && response.code < 400) {
                return new ByteArrayInputStream(response.body);
            }
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public InputStream getErrorStream() {
            if (response.code >= 400) {
                return new ByteArrayInputStream(response.body);
            }
            return null;
        }

        @Override
        public int getResponseCode() {
            return response.code;
        }

        @Override
        public OutputStream getOutputStream() {
            ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
            return new OutputStream() {
                @Override
                public void write(int b) {
                    requestBody.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    requestBody.write(b, off, len);
                }

                @Override
                public void close() {
                    LAST_REQUEST_BODIES.put(url.toString(), requestBody.toString(StandardCharsets.UTF_8));
                }
            };
        }

        @Override
        public void setRequestMethod(String method) throws ProtocolException {
            this.method = method;
        }

        @Override
        public void setDoOutput(boolean dooutput) {
            this.doOutput = dooutput;
        }

    }

    private static final class StubResponse {
        final int code;
        final byte[] body;

        StubResponse(int code, byte[] body) {
            this.code = code;
            this.body = body != null ? body : new byte[0];
        }
    }
}


