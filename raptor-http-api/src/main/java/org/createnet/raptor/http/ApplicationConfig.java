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

import org.createnet.raptor.http.exception.ApiExceptionMapper;
import org.createnet.raptor.http.filter.AuthorizationRequestFilter;
import org.createnet.raptor.http.filter.CORSResponseFilter;
import org.createnet.raptor.http.filter.LoggerResponseFilter;
import org.createnet.raptor.service.BaseAppConfig;
import org.createnet.raptor.service.tools.RaptorApplicationEventListener;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ApplicationConfig extends BaseAppConfig {

    public ApplicationConfig() {

        super();

        String resourcePackage = "org.createnet.raptor.http.api";
        packages(resourcePackage);
        
        
        register(AuthorizationRequestFilter.class);
        register(CORSResponseFilter.class);
        register(LoggerResponseFilter.class);
        register(ApiExceptionMapper.class);
        register(RaptorApplicationEventListener.class);
        
        registerDefault();
        registerSwagger(resourcePackage);
        
    }

}
