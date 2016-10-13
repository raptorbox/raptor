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

import io.swagger.jaxrs.config.BeanConfig;
import javax.inject.Singleton;
import org.createnet.raptor.http.exception.ApiExceptionMapper;
import org.createnet.raptor.http.filter.AuthorizationRequestFilter;
import org.createnet.raptor.http.filter.CORSResponseFilter;
import org.createnet.raptor.http.filter.LoggerResponseFilter;
import org.createnet.raptor.http.service.AuthService;
import org.createnet.raptor.http.service.ConfigurationService;
import org.createnet.raptor.http.service.DispatcherService;
import org.createnet.raptor.http.service.EventEmitterService;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.raptor.http.service.RaptorApplicationEventListener;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.raptor.http.service.TreeService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ApplicationConfig extends ResourceConfig {

  static public class AppBinder extends AbstractBinder {

    @Override
    protected void configure() {

      bind(ConfigurationService.class)
              .to(ConfigurationService.class)
              .in(Singleton.class);

      bind(StorageService.class)
              .to(StorageService.class)
              .in(Singleton.class);

      bind(IndexerService.class)
              .to(IndexerService.class)
              .in(Singleton.class);

      bind(AuthService.class)
              .to(AuthService.class)
              .in(Singleton.class);

      bind(DispatcherService.class)
              .to(DispatcherService.class)
              .in(Singleton.class);

      bind(EventEmitterService.class)
              .to(EventEmitterService.class)
              .in(Singleton.class);

      bind(TreeService.class)
              .to(TreeService.class)
              .in(Singleton.class);

    }
  }

  public ApplicationConfig() {

    super();   
    
    String resourcePackage = "org.createnet.raptor.http.api";
    packages(resourcePackage);

    register(io.swagger.jaxrs.listing.ApiListingResource.class);
    register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
    
    register(AuthorizationRequestFilter.class);
    register(CORSResponseFilter.class);
    register(LoggerResponseFilter.class);
    register(ApiExceptionMapper.class);
    register(RaptorApplicationEventListener.class);
    register(new AppBinder());    

    
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("3.0");
    beanConfig.setSchemes(new String[]{"http", "https"});
    beanConfig.setHost("api.raptor.local");
    beanConfig.setBasePath("/");
    beanConfig.setResourcePackage(resourcePackage);
    beanConfig.setScan(true);
    
    
  }

}
