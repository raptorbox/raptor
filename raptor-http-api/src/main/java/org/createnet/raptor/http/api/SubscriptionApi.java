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
package org.createnet.raptor.http.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/")
public class SubscriptionApi {

//  final private Logger logger = LoggerFactory.getLogger(SubscriptionApi.class);

//  @Inject
//  StorageService storage;
//
//  @Inject
//  IndexerService indexer;
//
//  @Inject
//  DispatcherService dispatcher;
//
//  @Inject
//  AuthService auth;
//  
  @GET
  @Path("{id}/streams/{streamId}/subscriptions")
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public Response load(){
    return Response.noContent().build();
  }
  
  @POST
  @Path("{id}/streams/{streamId}/subscriptions")
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public Response save(){
    return Response.noContent().build();
  }
  
  @DELETE
  @Path("subscriptions/{subscriptionId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public Response deleteSubscription(){
    return Response.noContent().build();
  }
  
  @GET
  @Path("subscriptions/{subscriptionId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public Response loadSubscription(){
    return Response.noContent().build();
  }
  
  @PUT
  @Path("subscriptions/{subscriptionId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public Response updateSubscription(){
    return Response.noContent().build();
  }
  
  
}
