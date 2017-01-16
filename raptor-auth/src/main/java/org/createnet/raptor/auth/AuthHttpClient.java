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
package org.createnet.raptor.auth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.Consts;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AuthHttpClient {

    final private Logger logger = LoggerFactory.getLogger(AuthHttpClient.class);

    protected int defaultTimeout = 3500; // default req timeout
    protected RequestConfig requestConfig = RequestConfig.DEFAULT;

//  protected RequestConfig requestConfig = RequestConfig.custom()
//              .setSocketTimeout(defaultTimeout)
//              .setConnectTimeout(defaultTimeout)
//              .setConnectionRequestTimeout(defaultTimeout)
//              .setRedirectsEnabled(true)
//              .build();

    private CloseableHttpClient httpclient;

    private CloseableHttpClient getHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, IOException {

        if (httpclient == null) {

            logger.debug("Created http client instance");

            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(
                            new File(config.token.truststore.path),
                            config.token.truststore.password.toCharArray(),
                            new TrustSelfSignedStrategy())
                    .build();

            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[]{"TLSv1.2", "TLSv1.1", "TLSv1"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());

            Registry socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", sslsf)
                    .build();

            HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            httpclient = HttpClients.custom()
                    //              .setSSLSocketFactory(sslsf)
                    .setConnectionManager(poolingConnManager)
                    //              .setConnectionManagerShared(true)
                    .build();
        }

        return httpclient;
    }

    public class ClientException extends RuntimeException {

        private int code = 0;
        private String reason;

        public int getCode() {
            return code;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String getMessage() {
            if (getCode() > 0) {
                return getCode() + " " + getReason();
            }
            return super.getMessage();
        }

        public ClientException(int code, String reason) {
            this.reason = reason;
            this.code = code;
        }

        public ClientException(Throwable t) {
            super(t);
        }

        public ClientException(String m, Throwable t) {
            super(m, t);
        }

        public ClientException(String m) {
            super(m);
        }

    }

    private AuthConfiguration config;

    private String checkUrl;
    private String syncUrl;
    private String loginUrl;

    public AuthHttpClient() {
    }

    public void setConfig(AuthConfiguration configuration) {

        this.config = configuration;

        this.syncUrl = configuration.token.syncUrl;
        this.checkUrl = configuration.token.checkUrl;
        this.loginUrl = configuration.token.loginUrl;

    }

    protected String checkToken(String token) {
        String prefix = "Bearer ";
        if (token.substring(0, prefix.length()).equals(prefix)) {
            return token;
        }
        logger.debug("Add bearer prefix to token {}", token);
        return prefix + token;
    }

    public String check(String accessToken, String payload) {

        logger.debug("Http client check request");

        accessToken = checkToken(accessToken);

        HttpPost httpost = new HttpPost(checkUrl);

        httpost.setConfig(requestConfig);
        httpost.setHeader("Authorization", accessToken);
        httpost.addHeader("Accept", "application/json");
        httpost.addHeader("Content-Type", "application/json");

        StringEntity entity = new StringEntity(payload, Consts.UTF_8);
        httpost.setEntity(entity);

        String response = null;

        CloseableHttpClient httpclient;
        try {
            httpclient = getHttpClient();
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException ex) {
            throw new ClientException(ex);
        }

        try (CloseableHttpResponse httpResponse = httpclient.execute(httpost)) {

            if (httpResponse.getStatusLine().getStatusCode() >= 400) {
                logger.warn("client.check() returned a faulty response: {} {}", httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
                throw new ClientException(
                        httpResponse.getStatusLine().getStatusCode(),
                        httpResponse.getStatusLine().getReasonPhrase()
                );
            }

            InputStream inputStream = httpResponse.getEntity().getContent();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            //close the response
            httpResponse.close();

            response = result.toString(Consts.UTF_8.toString());
            logger.debug("Http client response for check: {}", response);

        } catch (IOException ex) {
            logger.error("Http client error for check", ex);
            throw new ClientException(ex);
        } finally {
            logger.debug("release client connection");
            httpost.releaseConnection();
        }

        return response;
    }

    public void sync(String accessToken, String body) {

        logger.debug("Http client sync request");

        accessToken = checkToken(accessToken);

        HttpPost httpost = new HttpPost(syncUrl);

        httpost.addHeader("Authorization", accessToken);
        httpost.addHeader("Accept", "application/json");
        httpost.addHeader("Content-Type", "application/json");

        StringEntity entity = new StringEntity(body, Consts.UTF_8);
        httpost.setEntity(entity);
        httpost.setConfig(requestConfig);

        CloseableHttpClient httpclient;
        try {
            httpclient = getHttpClient();
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException ex) {
            logger.error("Cannot get client", ex);
            throw new ClientException(ex);
        }

        try (CloseableHttpResponse httpResponse = httpclient.execute(httpost)) {

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

            //close the response
            httpResponse.close();

            if (statusCode >= 400) {
                logger.debug("Http request failed for sync ");
                throw new ClientException(statusCode, statusMessage);
            }

        } catch (ClientException | IOException ex) {
            logger.error("Http request error for sync: {}", ex.getMessage());
            throw new ClientException(ex);
        } finally {
            logger.debug("Http request release connection for sync");
            httpost.releaseConnection();
        }
    }

    public String login(String body) {

        logger.debug("Http client login request");

        HttpPost httpost = new HttpPost(loginUrl);

        httpost.addHeader("Accept", "application/json");
        httpost.addHeader("Content-Type", "application/json");

        StringEntity entity = new StringEntity(body, Consts.UTF_8);
        httpost.setEntity(entity);
        httpost.setConfig(requestConfig);

        CloseableHttpClient httpclient;
        try {
            httpclient = getHttpClient();
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException ex) {
            logger.error("Cannot get client", ex);
            throw new ClientException(ex);
        }

        try (CloseableHttpResponse httpResponse = httpclient.execute(httpost)) {

            if (httpResponse.getStatusLine().getStatusCode() >= 400) {
                logger.warn("client.check() returned a faulty response: {} {}", httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
                throw new ClientException(
                        httpResponse.getStatusLine().getStatusCode(),
                        httpResponse.getStatusLine().getReasonPhrase()
                );
            }

            InputStream inputStream = httpResponse.getEntity().getContent();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            //close the response
            httpResponse.close();

            String response = result.toString(Consts.UTF_8.toString());
            logger.debug("Http client response for check: {}", response);

            return response;

        } catch (IOException ex) {
            logger.error("Http client error for check", ex);
            throw new ClientException(ex);
        } finally {
            logger.debug("release client connection");
            httpost.releaseConnection();
        }

    }

}
