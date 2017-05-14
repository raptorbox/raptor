/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.sdk;

import org.createnet.raptor.sdk.AbstractClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import java.io.IOException;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.exception.ClientException;
import org.createnet.raptor.sdk.exception.MissingAuthenticationException;
import org.createnet.raptor.models.exception.RequestException;
import org.createnet.raptor.sdk.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpClient class for MQTT and HTTP operations
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class HttpClient extends AbstractClient {

    static {

        // Only one time
        Unirest.setObjectMapper(new ObjectMapper() {

            private final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            @Override
            public <T> T readValue(String value, Class<T> valueType) {
                if (value == null) {
                    return null;
                }
                if (value.isEmpty()) {
                    return null;
                }
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    final protected Logger logger = LoggerFactory.getLogger(HttpClient.class);

    public HttpClient(Raptor container) {
        super(container);
    }

    /**
     * Add the configured base url to path
     *
     * @param path base path to create url from
     * @return a full url
     */
    public String url(String path) {
        return getConfig().getUrl() + path;
    }

    protected String getToken() {
        String token = getContainer().Auth().getToken();
        if (token == null) {
            throw new MissingAuthenticationException("Token is not available");
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    protected void prepareRequest() {
        //
    }

    protected HttpRequestWithBody createRequest(HttpMethod method, String url, RequestOptions opts) {

        assert opts != null;

        HttpRequestWithBody req = new HttpRequestWithBody(method, getClient().url(url));

        if (opts.withAuth()) {
            req.header("Authorization", getToken());
        }

        req.header("Content-Type", opts.withTextBody() ? "text/plain" : "application/json");

        return req;
    }

    protected void checkResponse(HttpResponse<?> response) {

        int status = response.getStatus();

        if (status >= 400) {
            String message = "";
            if (response.getBody() != null) {
                message = response.getBody().toString();
                if (response.getBody() instanceof JsonNode) {
                    JsonNode err = (JsonNode) response.getBody();
                    if (err.has("message")) {
                        message = err.get("message").asText();
                    }
                }
            }
            logger.error("Request failed {} {}: {}", response.getStatus(), response.getStatusText(), message);
            throw new RequestException(response.getStatus(), response.getStatusText(), message);
        }

    }

    protected JsonNode tryRequest(HttpRequest req, RequestOptions opts) {

        int tries = 0;
        int maxRetry = opts.withRetry() ? opts.getMaxRetry() : 1;
        int waitFor = opts.getWaitFor();

        while (tries < maxRetry) {
            try {
                HttpResponse<JsonNode> objResponse = req.asObject(JsonNode.class);
                checkResponse(objResponse);
                return objResponse.getBody();

            } catch (RequestException ex) {

                tries++;

                if (!opts.withRetry() || tries == maxRetry) {
                    logger.error("Request failed after {} tries. Last exception: {}", tries, ex.getMessage());
                    throw ex;
                }

                logger.warn("Request error: {}. Retrying {}/{}", ex.getMessage(), tries, maxRetry);

                try {
                    Thread.sleep(waitFor);
                } catch (InterruptedException ex1) {
                }

            } catch (UnirestException ex) {
                logger.error("HTTP Client error: {}", ex.getMessage());
                throw new ClientException(ex);
            }
        }

        throw new ClientException("Request failed");
    }

    /**
     * Perform a request with body to the API
     *
     * @param httpMethod
     * @param url path of request
     * @param body content to be sent
     * @param opts
     * @return the request response
     */
    public JsonNode request(HttpMethod httpMethod, String url, JsonNode body, RequestOptions opts) {

        logger.debug("{} {}", httpMethod.name(), url);

        if (opts == null) {
            opts = RequestOptions.defaults();
        }

        prepareRequest();
        HttpRequestWithBody req = createRequest(httpMethod, url, opts);

        if (body != null) {
            req.body(body);
        }

        return tryRequest(req, opts);
    }

    /**
     * Perform a request with body to the API
     *
     * @param httpMethod
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    public JsonNode request(HttpMethod httpMethod, String url, JsonNode body) {
        return request(httpMethod, url, body, null);
    }

    /**
     * Perform a request to the API without a body
     *
     * @param httpMethod
     * @param url path of request
     * @param opts
     * @return the request response
     */
    public JsonNode request(HttpMethod httpMethod, String url, RequestOptions opts) {
        return request(httpMethod, url, null, opts);
    }

    /**
     * Perform a request to the API without a body
     *
     * @param httpMethod
     * @param url path of request
     * @return the request response
     */
    public JsonNode request(HttpMethod httpMethod, String url) {
        return request(httpMethod, url, null, null);
    }

    /**
     * Perform a request to the API with a text/plain body
     *
     * @param httpMethod
     * @param url path of request
     * @param body
     * @param opts
     * @return the request response
     */
    public JsonNode requestAsText(HttpMethod httpMethod, String url, String body, RequestOptions opts) {
        logger.debug("{} text/plain {}", httpMethod, url);
        HttpRequestWithBody req = createRequest(httpMethod, url, opts);
        req.body(body);
        return tryRequest(req, opts);
    }

    /**
     * Send a text payload (specific for invoking actions)
     *
     * @param url
     * @param payload
     * @return
     */
    public JsonNode post(String url, String payload) {
        return requestAsText(HttpMethod.POST, url, payload, RequestOptions.defaults().textBody(true));
    }

    /**
     * Send a text payload (specific for invoking actions)
     *
     * @param url
     * @param payload
     * @return
     */
    public JsonNode put(String url, String payload) {
        return requestAsText(HttpMethod.PUT, url, payload, RequestOptions.defaults().textBody(true));
    }

    /**
     * Get a text payload (specific for retrieving actions status)
     *
     * @param url
     * @param payload
     * @return
     */
    public JsonNode get(String url, String payload) {
        return requestAsText(HttpMethod.GET, url, null, RequestOptions.retriable().textBody(true));
    }

    /**
     * Perform a PUT request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @param opts
     * @return the request response
     */
    public JsonNode put(String url, JsonNode body, RequestOptions opts) {
        return request(HttpMethod.PUT, url, body, opts);
    }

    /**
     * Perform a POST request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @param opts
     * @return the request response
     */
    public JsonNode post(String url, JsonNode body, RequestOptions opts) {
        return request(HttpMethod.POST, url, body, opts);
    }

    /**
     * Perform a GET request to the API
     *
     * @param url path of request
     * @param opts
     * @return the request response
     */
    public JsonNode get(String url, RequestOptions opts) {
        return request(HttpMethod.GET, url, opts);
    }

    /**
     * Perform a DELETE request to the API
     *
     * @param url path of request
     * @param opts
     * @return the request response
     */
    public JsonNode delete(String url, RequestOptions opts) {
        return request(HttpMethod.DELETE, url, opts);
    }

    /**
     * Perform a PUT request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    public JsonNode put(String url, JsonNode body) {
        return put(url, body, RequestOptions.defaults());
    }

    /**
     * Perform a POST request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    public JsonNode post(String url, JsonNode body) {
        return post(url, body, RequestOptions.defaults());
    }

    /**
     * Perform a GET request to the API
     *
     * @param url path of request
     * @return the request response
     */
    public JsonNode get(String url) {
        return get(url, RequestOptions.retriable());
    }

    /**
     * Perform a DELETE request to the API
     *
     * @param url path of request
     * @return the request response
     */
    public JsonNode delete(String url) {
        return delete(url, RequestOptions.defaults());
    }

}
