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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.http.service.RaptorService;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class HttpService {
  
  final private Logger logger = LoggerFactory.getLogger(HttpService.class);
  
  protected String defaultURI = "http://127.0.0.1:8080/";
  
  private HttpServer server;
  
  public void start(String uri) throws IOException {
    startServer(uri);
  }
  
  public void start() throws IOException {
    startServer();
  }
  
  public void stop() throws IOException {
    stopServer(0);
  }
  
  public void stop(int i) throws IOException {
    stopServer(i);
  }
  
  private HttpServer startServer() throws IOException {
    return startServer(getURI());
  }
  
  private HttpServer startServer(String uri) throws IOException {
        
    URI serviceURI = UriBuilder.fromUri(uri).build();
    
    logger.debug("Starting HTTP service");
            
    // default to port 8080
    server = HttpServer.create(new InetSocketAddress(serviceURI.getHost(), serviceURI.getPort()), 0);
    HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(new ApplicationConfig(), HttpHandler.class);
    
    server.createContext(serviceURI.getPath(), handler);
    server.start();

    logger.info("HTTP service running at {}. Hit enter to stop.", serviceURI.toString());
    
    return server;
  }

  private void stopServer(int i) {
    server.stop(i);
    logger.debug("Stopped HTTP service");
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void main(String[] args) throws IOException {

    HttpService launcher = new HttpService();
    launcher.startServer();
    
    System.in.read();
    launcher.stopServer(0);
  }

  private String getURI() {
    String uri = System.getProperty("uri");
    return uri == null ? defaultURI : uri;
  }
  
}
