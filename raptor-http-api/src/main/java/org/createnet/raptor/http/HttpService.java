/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.http;

import org.createnet.raptor.service.ServiceBinder;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class HttpService {

    final private Logger logger = LoggerFactory.getLogger(HttpService.class);

    protected String defaultURI = "http://127.0.0.1:8080/";

    private HttpServer server;

    public void start(String uri) throws Exception {
        startServer(uri);
    }

    public void start() throws Exception {
        startServer();
    }

    public void stop() throws Exception {
        stopServer();
    }

    private HttpServer startServer() throws Exception {
        return startServer(getURI());
    }

    private HttpServer startServer(String uri) throws IOException, Exception {

        URI serviceURI = UriBuilder.fromUri(uri).build();

        logger.debug("Starting HTTP service");
        HttpServer server;
        try {
            
            ApplicationConfig appconfig = new ApplicationConfig();
            server = GrizzlyHttpServerFactory.createHttpServer(serviceURI, appconfig);

        } catch (Exception e) {
            logger.error("Cannot start http service: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        server.start();

        logger.info("HTTP service running at {}", serviceURI.toString());

        return server;
    }

    private void stopServer() throws Exception {
        server.shutdown();
        logger.debug("Stopped HTTP service");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) throws Exception {

        HttpService launcher = new HttpService();
        HttpServer server = launcher.startServer();
    }

    private String getURI() {
        String uri = System.getProperty("uri");
        return uri == null ? defaultURI : uri;
    }

}
