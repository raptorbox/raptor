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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.search.raptor.search.Indexer;
import org.createnet.raptor.config.exception.ConfigurationException;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/{id}/children")
@Api
public class ObjectTreeApi extends AbstractApi {

  final private Logger logger = LoggerFactory.getLogger(ObjectTreeApi.class);

  @GET
  @ApiOperation(value = "Return the device children if any", notes = "")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found")
  })
  public List<String> children(@PathParam("id") String id) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Indexer.IndexerException, Authentication.AuthenticationException {

    logger.debug("Load object {}", id);
    ServiceObject obj = loadObject(id);

    if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
      throw new ForbiddenException("Cannot read object");
    }

    List<ServiceObject> list = indexer.getChildren(obj);

    return list.stream().map((ServiceObject o)-> { 
        try {
            return o.toJSON();
        } catch (RaptorComponent.ParserException ex) {
            return null;
        }
        })
        .filter(o -> o != null)
        .collect(Collectors.toList());
  }

  @POST
  @ApiOperation(value = "Add a device as children", notes = "")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Ok"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found")
  })
  public List<String> setChildren(@PathParam("id") String id, List<String> childrenId) throws Storage.StorageException, RaptorComponent.ParserException, ConfigurationException, Authorization.AuthorizationException, Indexer.IndexerException, Authentication.AuthenticationException, RaptorComponent.ValidationException {

    logger.debug("Load object {}", id);
    ServiceObject obj = loadObject(id);
    
    if (!auth.isAllowed(obj, Authorization.Permission.Update)) {
      throw new ForbiddenException("Cannot update object");
    }

    List<ServiceObject> children = indexer.getObjects(childrenId);
    children.forEach((ServiceObject o) -> {
        o.parentId = id;
    });

    indexer.saveObjects(children);
    storage.saveObjects(children);
    
    List<String> list = indexer.getChildrenList(obj);

    childrenId.stream().forEach((String childId)-> {
        if(!list.contains(childId)) {
            list.add(childId);
        }
    });
    
    indexer.setChildrenList(obj, list);
    
    return children(id);
  }

}
