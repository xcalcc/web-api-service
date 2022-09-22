/*
   Copyright (C) 2019-2022 Xcalibyte (Shenzhen) Limited.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.xcal.api.config;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.regex.Pattern;

/**
 * A servlet filter that logs incoming HTTP requests. This approach is similar to the Spring CommonsRequestLoggingFilter, but is customized to ensure that the
 * full request body is always read and logged. In addition, this filter only has the concept of "before" request logging.
 */
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    public static final String DEFAULT_LOG_MESSAGE_PREFIX = "HTTP Request [";
    public static final String DEFAULT_LOG_MESSAGE_SUFFIX = "]";
    private static final Integer DEFAULT_MAX_PAYLOAD_LENGTH = 10000; // Default to unlimited.

    private static final String CURL_PREFIX = "CURL";
    private static final String PROMETHEUS_PREFIX = "PROMETHEUS";

    private boolean includeQueryString = true;
    private boolean includeClientInfo = true;
    private boolean includeHeaders = true;
    private boolean includePayload = true;
    private Integer maxPayloadLength = DEFAULT_MAX_PAYLOAD_LENGTH;
    private String logMessagePrefix = DEFAULT_LOG_MESSAGE_PREFIX;
    private String logMessageSuffix = DEFAULT_LOG_MESSAGE_SUFFIX;

    /**
     * Set whether or not the query string should be included in the log message.
     */
    public void setIncludeQueryString(boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    /**
     * Return whether or not the query string should be included in the log message.
     */
    protected boolean isIncludeQueryString() {
        return this.includeQueryString;
    }

    /**
     * Set whether or not the client address and session id should be included in the log message.
     */
    public void setIncludeClientInfo(boolean includeClientInfo) {
        this.includeClientInfo = includeClientInfo;
    }

    /**
     * Return whether or not the client address and session id should be included in the log message.
     */
    protected boolean isIncludeClientInfo() {
        return this.includeClientInfo;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    protected boolean isIncludeHeaders() {
        return this.includeHeaders;
    }

    /**
     * Set whether or not the request payload (body) should be included in the log message.
     */
    public void setIncludePayload(boolean includePayload) {
        this.includePayload = includePayload;
    }

    /**
     * Return whether or not the request payload (body) should be included in the log message.
     */
    protected boolean isIncludePayload() {
        return includePayload;
    }

    /**
     * Sets the maximum length of the payload body to be included in the log message. Default (i.e. null) is unlimited characters.
     */
    public void setMaxPayloadLength(Integer maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }

    /**
     * Return the maximum length of the payload body to be included in the log message.
     */
    protected Integer getMaxPayloadLength() {
        return maxPayloadLength;
    }

    /**
     * Set the value that should be prepended to the log message.
     */
    public void setLogMessagePrefix(String logMessagePrefix) {
        this.logMessagePrefix = logMessagePrefix;
    }

    /**
     * Set the value that should be appended to the log message.
     */
    public void setLogMessageSuffix(String logMessageSuffix) {
        this.logMessageSuffix = logMessageSuffix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest requestLocal = request;

        // Determine if this is the first request or not. We only want to wrap the request to log on the first request.
        try {
            boolean isFirstRequest = !isAsyncDispatch(requestLocal);
            if (isFirstRequest) {
                requestLocal = new RequestLoggingFilterWrapper(requestLocal);
                ((RequestLoggingFilterWrapper) requestLocal).logRequest(request);
            }
        }catch(Exception e){ //any error for logging should not affect the business flow
            LOGGER.error("[logRequest] Exception logging http request metadata: ", e);
        }
        // Move onto the next filter while wrapping the request with our own custom logging class.
        filterChain.doFilter(requestLocal, response);
    }

    /**
     * A request wrapper that logs incoming requests.
     */
    public class RequestLoggingFilterWrapper extends HttpServletRequestWrapper {
        private byte[] payload = null;
        private BufferedReader reader;

        /**
         * Constructs a request logging filter wrapper.
         *
         * @param request the request to wrap.
         * @throws IOException if any problems were encountered while reading from the stream.
         */
        public RequestLoggingFilterWrapper(HttpServletRequest request) throws IOException {
            // Perform super class processing.
            super(request);

            // Only grab the payload if debugging is enabled. Otherwise, we'd always be pre-reading the entire payload for no reason which cause a slight
            // performance degradation for no reason.
            if (LOGGER.isInfoEnabled()) {
                // Read the original payload into the payload variable.
                InputStream inputStream = null;
                try {
                    // Get the input stream.
                    inputStream = request.getInputStream();
                    if (inputStream != null) {
                        // Read the payload from the input stream.
                        payload = IOUtils.toByteArray(request.getInputStream());
                    }
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException iox) {
                            LOGGER.warn("Unable to close request input stream.", iox);
                        }
                    }
                }
            }
        }

        /**
         * Log the request message.
         *
         * @param request the request.
         */
        public void logRequest(HttpServletRequest request) {
            String agent = request.getHeader("user-agent");

            if (null !=agent
                    && !agent.toUpperCase().contains(CURL_PREFIX)
                    && !agent.toUpperCase().contains(PROMETHEUS_PREFIX)) {

                StringBuilder message = new StringBuilder();

                // Append the log message prefix.
                message.append(logMessagePrefix);

                // Append the URI.
                message.append("uri=").append(request.getRequestURI());

                // Append the query string if present.
                if (isIncludeQueryString() && StringUtils.hasText(request.getQueryString())) {
                    message.append('?').append(request.getQueryString());
                }

                // Append the HTTP method.
                message.append(";method=").append(request.getMethod());

                // Append the client information.
                if (isIncludeClientInfo()) {
                    // The client remote address.
                    String client = request.getRemoteAddr();
                    if (StringUtils.hasLength(client)) {
                        message.append(";client=").append(client);
                    }

                    // The HTTP session information.
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        message.append(";session=").append(session.getId());
                    }

                    // The remote user information.
                    String user = request.getRemoteUser();
                    if (user != null) {
                        message.append(";user=").append(user);
                    }
                }

                if (isIncludeHeaders()) {
                    message.append(";headers=").append((new ServletServerHttpRequest(request)).getHeaders());
                }

                // Get the request payload.
                String payloadString = "";
                try {
                    if (payload != null) {
                        LOGGER.debug("[logRequest] payload.length {}", payload.length);
                        message.append(";payload.length=").append(payload.length);
                        if (payload.length > 0 && payload.length < DEFAULT_MAX_PAYLOAD_LENGTH /*prevent overflow*/) {
                            payloadString = new String(payload,
                                    0,
                                    payload.length,
                                    getCharacterEncoding())
                                    .replace("\n", "")
                                    .replace("\r", "");
                        } else if (payload.length >= DEFAULT_MAX_PAYLOAD_LENGTH) {
                            LOGGER.warn("[logRequest] payload will not be logged as payload length {} >= limit {}", payload.length, DEFAULT_MAX_PAYLOAD_LENGTH);
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    LOGGER.warn("[logRequest] UnsupportedEncodingException during extracting payload for logging: {}", e.getMessage());
                    payloadString = "[Unknown]";
                } catch (Exception e) {
                    LOGGER.error("[logRequest] Exception during extracting payload for logging: ", e);
                }

                // Append the request payload if present.
                if (isIncludePayload() && StringUtils.hasLength(payloadString)) {
                    String sanitizedPayloadString = payloadString;
                    /*
                     * Replaces the payload if it contains the word "password" for requests to jobDefinitions and jobs.
                     */
                    if (request.getRequestURI().endsWith("/login")) {
                        Pattern pattern = Pattern.compile("password", Pattern.CASE_INSENSITIVE);
                        if (pattern.matcher(payloadString).find()) {
                            sanitizedPayloadString = "<***sanitized***>";
                        }
                    }
                    /*
                     * Limit logged payload length if max length is set
                     */
                    else if (getMaxPayloadLength() != null) {
                        sanitizedPayloadString = payloadString.substring(0,
                                Math.min(payloadString.length(),
                                        getMaxPayloadLength()));
                    }

                    message.append(";payload=").append(sanitizedPayloadString);
                }

                // Append the log message suffix.
                message.append(logMessageSuffix);

                // Log the actual message.
                LOGGER.info(message.toString());
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (payload == null) {
                // If no payload is present (i.e. debug logging isn't enabled), then perform the standard super class functionality.
                return super.getInputStream();
            } else {
                return new ServletInputStream() {
                    private int lastIndexRetrieved = -1;
                    private ReadListener readListener = null;

                    @Override
                    public boolean isFinished() {
                        return (lastIndexRetrieved == payload.length - 1);
                    }

                    @Override
                    public boolean isReady() {
                        // This implementation will never block
                        // We also never need to call the readListener from this method, as this method will never return false
                        return isFinished();
                    }

                    @Override
                    public void setReadListener(ReadListener readListener) {
                        this.readListener = readListener;
                        if (!isFinished()) {
                            try {
                                readListener.onDataAvailable();
                            } catch (IOException e) {
                                readListener.onError(e);
                            }
                        } else {
                            try {
                                readListener.onAllDataRead();
                            } catch (IOException e) {
                                readListener.onError(e);
                            }
                        }
                    }

                    @Override
                    public int read() throws IOException {
                        int i;
                        if (!isFinished()) {
                            i = payload[lastIndexRetrieved + 1];
                            lastIndexRetrieved++;
                            if (isFinished() && (readListener != null)) {
                                try {
                                    readListener.onAllDataRead();
                                } catch (IOException ex) {
                                    readListener.onError(ex);
                                    throw ex;
                                }
                            }
                            return i;
                        } else {
                            return -1;
                        }
                    }
                };
            }
        }

        @Override
        public int getContentLength() {
            if (payload == null) {
                return super.getContentLength();
            } else {
                return payload.length;
            }
        }

        @Override
        public String getCharacterEncoding() {
            String enc = super.getCharacterEncoding();
            return (enc != null ? enc : WebUtils.DEFAULT_CHARACTER_ENCODING);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (payload == null) {
                return super.getReader();
            } else {
                if (reader == null) {
                    this.reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(payload), getCharacterEncoding()));
                }
                return reader;
            }
        }
    }
}