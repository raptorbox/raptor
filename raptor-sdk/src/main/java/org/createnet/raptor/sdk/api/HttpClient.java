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
package org.createnet.raptor.sdk.api;

import org.createnet.raptor.sdk.AbstractClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import java.io.IOException;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.exception.ClientException;
import org.createnet.raptor.sdk.exception.MissingAuthenticationException;
import org.createnet.raptor.models.exception.RequestException;
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
     * List of base path for Raptor API
     */
    public static class Routes {

        final static public String LIST = "/";
        final static public String SEARCH = "/search";

        final static public String CREATE = LIST;
        final static public String UPDATE = "/%s";
        final static public String LOAD = UPDATE;
        final static public String DELETE = UPDATE;

        final static public String STREAM = "/%s/streams/%s";
        final static public String PUSH = STREAM;
        final static public String LAST_UPDATE = PUSH;
        final static public String PULL = PUSH + "/list";
        final static public String SEARCH_DATA = PUSH + "/search";

        final static public String INVOKE = "/%s/actions/%s";
        final static public String ACTION_STATUS = INVOKE;
        final static public String ACTION_LIST = "/%s/actions";
        
        final static public String SUBSCRIBE_ACTION = INVOKE;
        final static public String SUBSCRIBE_STREAM = PUSH;

        final static public String LOGIN = "/auth/login";
        final static public String LOGOUT = LOGIN;
        final static public String REFRESH_TOKEN = "/auth/refresh";
        
        final static public String USER_CREATE = "/auth/user";
        final public static String USER_GET = USER_CREATE + "/%s";
        final public static String USER_UPDATE = USER_GET;
        final public static String USER_DELETE = USER_GET;
        final public static String USER_GET_ME = "/auth/me";        
        final public static String USER_UPDATE_ME = USER_GET_ME;
        
        final static public String TOKEN_CREATE = "/auth/token";
        final static public String TOKEN_UPDATE = TOKEN_CREATE + "/%s";
        final static public String TOKEN_DELETE = TOKEN_UPDATE;
        final static public String TOKEN_GET = TOKEN_CREATE + "?uuid=%s";
        
        final static public String PREFERENCES_GET_ALL = "/profile/%s";
        final static public String PREFERENCES_GET = PREFERENCES_GET_ALL + "/%s";
        final static public String PREFERENCES_SET = PREFERENCES_GET;
        final static public String PREFERENCES_DELETE = PREFERENCES_GET;
        
        final static public String TOKEN_PERMISSION_GET = "/auth/permission/token/%s";
        final static public String TOKEN_PERMISSION_BY_USER = TOKEN_PERMISSION_GET + "/%s";
        final static public String TOKEN_PERMISSION_SET = TOKEN_PERMISSION_GET;
        
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
        return "Bearer " + token;
    }

    protected void prepareRequest() {
        //
    }

    protected HttpRequestWithBody request(HttpMethod method, String url) {
        return request(method, url, true, true);
    }

    protected HttpRequestWithBody request(HttpMethod method, String url, boolean auth, boolean json) {
        HttpRequestWithBody req = new HttpRequestWithBody(method, getClient().url(url));
        if (auth) {
            req.header("Authorization", getToken());
        }
        if (json) {
            req.header("Content-Type", "application/json");
        }
        return req;
    }

    protected void checkResponse(HttpResponse<?> response) {

        int status = response.getStatus();

        if (status >= 400) {
            String message = "";
            if(response.getBody() != null) {
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

    /**
     * Perform a PUT request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    public JsonNode put(String url, JsonNode body) {
        try {
            logger.debug("PUT {}", url);
            prepareRequest();
            HttpResponse<JsonNode> objResponse = request(HttpMethod.PUT, url)
                    .body(body)
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a POST request to the API
     *
     * @param url path of request
     * @param body content to be sent
     * @return the request response
     */
    public JsonNode post(String url, JsonNode body) {
        try {
            logger.debug("POST {}", url);
            // catch login url and skip token signing
            prepareRequest();
            HttpResponse<JsonNode> objResponse = request(HttpMethod.POST, url, !url.equals(Routes.LOGIN), true)
                    .body(body)
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a GET request to the API
     *
     * @param url path of request
     * @return the request response
     */
    public JsonNode get(String url) {
        try {
            logger.debug("GET {}", url);
            prepareRequest();
            HttpResponse<JsonNode> objResponse = request(HttpMethod.GET, url)
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

    /**
     * Perform a DELETE request to the API
     *
     * @param url path of request
     * @return the request response
     */
    public JsonNode delete(String url) {
        try {
            logger.debug("DELETE {}", url);
            prepareRequest();
            HttpResponse<JsonNode> objResponse = request(HttpMethod.DELETE, url)
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

    /**
     * Send a text payload (specific for invoking actions)
     *
     * @param url
     * @param payload
     * @return
     */
    public JsonNode post(String url, String payload) {
        try {
            logger.debug("POST text/plain {}", url);
            HttpResponse<JsonNode> objResponse = request(HttpMethod.POST, url, true, false)
                    .header("content-type", "text/plain")
                    .body(payload)
                    .asObject(JsonNode.class);
            checkResponse(objResponse);
            return objResponse.getBody();
        } catch (UnirestException ex) {
            logger.error("Request error: {}", ex.getMessage());
            throw new ClientException(ex);
        }
    }

}
