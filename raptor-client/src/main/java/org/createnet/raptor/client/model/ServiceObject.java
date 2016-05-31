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
package org.createnet.raptor.client.model;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.createnet.raptor.client.RaptorClient;
import org.createnet.raptor.client.RaptorComponent;
import org.createnet.raptor.client.exception.ClientException;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ServiceObject
        extends org.createnet.raptor.models.objects.ServiceObject
        implements RaptorComponent {

  private RaptorClient client;

  public ServiceObject() {
  }

  public ServiceObject(String id) {
    this.id = id;
  }

  public ServiceObject load(String id) throws ClientException {

    try {

      HttpResponse<ServiceObject> objResponse = Unirest
              .get(getClient().url(RaptorClient.Routes.LOAD))
              .routeParam("id", id)
              .asObject(ServiceObject.class);

      ServiceObject obj = objResponse.getBody();
      return obj;

    } catch (UnirestException ex) {
      throw new ClientException(ex);
    }

  }

  public ServiceObject update() throws ClientException {

    try {

      HttpResponse<ServiceObject> objResponse = Unirest
              .get(getClient().url(RaptorClient.Routes.UPDATE))
              .routeParam("id", id)
              .asObject(ServiceObject.class);

      return this;

    } catch (UnirestException ex) {
      throw new ClientException(ex);
    }

  }
  
  public ServiceObject delete() throws ClientException {

    try {

      HttpResponse<ServiceObject> objResponse = Unirest
              .get(getClient().url(RaptorClient.Routes.UPDATE))
              .routeParam("id", id)
              .asObject(ServiceObject.class);

      return getClient().createObject();

    } catch (UnirestException ex) {
      throw new ClientException(ex);
    }

  }

  public ServiceObject load() throws ClientException {
    if (this.getId() == null) {
      throw new ClientException("ServiceObject is missing id, cannot load");
    }
    return load(this.getId());
  }
  
  public RaptorClient getClient() {
    return this.client;
  }

  @Override
  public void setClient(RaptorClient client) {
    this.client = client;
  }

}
