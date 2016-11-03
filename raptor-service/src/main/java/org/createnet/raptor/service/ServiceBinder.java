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
package org.createnet.raptor.service;

import javax.inject.Singleton;
import org.createnet.raptor.service.core.ActionManagerService;
import org.createnet.raptor.service.core.ObjectManagerService;
import org.createnet.raptor.service.core.StreamManagerService;
import org.createnet.raptor.service.tools.AuthService;
import org.createnet.raptor.service.tools.CacheService;
import org.createnet.raptor.service.tools.ConfigurationService;
import org.createnet.raptor.service.tools.DispatcherService;
import org.createnet.raptor.service.tools.EventEmitterService;
import org.createnet.raptor.service.tools.IndexerService;
import org.createnet.raptor.service.tools.StorageService;
import org.createnet.raptor.service.tools.TreeService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Define binding to service classes
 * 
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ServiceBinder extends AbstractBinder {

    @Override
    protected void configure() {

        bind(EventEmitterService.class)
                .to(EventEmitterService.class)
                .in(Singleton.class);

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

        bind(TreeService.class)
                .to(TreeService.class)
                .in(Singleton.class);

        bind(CacheService.class)
                .to(CacheService.class)
                .in(Singleton.class);
        
        // Data & Object managers
        
        bind(ObjectManagerService.class)
                .to(ObjectManagerService.class)
                .in(Singleton.class);
        bind(StreamManagerService.class)
                .to(StreamManagerService.class)
                .in(Singleton.class);
        bind(ActionManagerService.class)
                .to(ActionManagerService.class)
                .in(Singleton.class);

        
        
    }
}
