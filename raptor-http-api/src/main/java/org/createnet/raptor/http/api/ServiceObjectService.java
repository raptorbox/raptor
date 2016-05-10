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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.db.Storage;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/")
public class ServiceObjectService {
  
  final private Logger logger = LoggerFactory.getLogger(ServiceObjectService.class);
  
  @Inject StorageService storage;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ServiceObject loadObjects() {
    ServiceObject obj = new ServiceObject();
    return obj;
  }

  @GET @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public ServiceObject loadObject(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException {
    
    logger.debug("Load object {}", id);
    
    ServiceObject obj = storage.getObject(id);
    
    return obj;
  }

}
