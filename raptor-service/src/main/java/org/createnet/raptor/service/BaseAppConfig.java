/*
 * Copyright 2017 FBK/CREATE-NET
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
package org.createnet.raptor.service;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Info;
import io.swagger.models.License;
import org.createnet.raptor.service.tools.RaptorApplicationEventListener;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
abstract public class BaseAppConfig extends ResourceConfig {

    public void registerDefault() {
        register(RaptorApplicationEventListener.class);
        register(new ServiceBinder());
    }

    public void registerSwagger(String resourcePackage) {

        register(io.swagger.jaxrs.listing.ApiListingResource.class);
        register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setTitle("Raptor HTTP API");
        beanConfig.setVersion("3.0");
        beanConfig.setSchemes(new String[]{"http", "https"});
        beanConfig.setHost("api.raptor.local");

        Info info = new Info();
        info.setTitle("Raptor API");
        info.setDescription("The IoT platform");
        info.setLicense(
                new License()
                        .name("Apache v2")
                        .url("https://raw.githubusercontent.com/raptorbox/raptor/master/LICENSE")
        );
        info.setVersion("3.0");

        beanConfig.setInfo(info);
        beanConfig.setBasePath("/");
        beanConfig.setResourcePackage(resourcePackage);
        beanConfig.setScan(true);
    }

}
