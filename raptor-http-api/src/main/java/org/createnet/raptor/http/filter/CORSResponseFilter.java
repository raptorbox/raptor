/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
package org.createnet.raptor.http.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class CORSResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response)
          throws IOException {

    response.getHeaders().add("X-Powered-By", "Raptor");

//    if (request.getMethod().toUpperCase().equals("OPTIONS")) {
      
      response.getHeaders().add("Access-Control-Allow-Origin", "*");
      response.getHeaders().add("Access-Control-Allow-Headers", "Authorization,Content-Type,X-Requested-With");
//      response.getHeaders().add("Access-Control-Allow-Credentials", "true");
      response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

//    }

  }
}
