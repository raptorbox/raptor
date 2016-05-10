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

import javax.inject.Singleton;
import org.createnet.raptor.http.exception.ExceptionMapper;
import org.createnet.raptor.http.filter.AuthorizationRequestFilter;
import org.createnet.raptor.http.service.AuthService;
import org.createnet.raptor.http.service.ConfigurationService;
import org.createnet.raptor.http.service.StorageService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ApplicationConfig extends ResourceConfig {

  public ApplicationConfig() {

    register(AuthorizationRequestFilter.class);
    register(ExceptionMapper.class);
    
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(ConfigurationService.class).to(ConfigurationService.class).in(Singleton.class);
        bind(StorageService.class).to(StorageService.class).in(Singleton.class);
        bind(AuthService.class).to(AuthService.class).in(Singleton.class);
      }
    });
    
    packages(true, "org.createnet.raptor.http.api");

  }

}
