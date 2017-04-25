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
package org.createnet.raptor.auth.service.services;

import org.createnet.raptor.config.ConfigurationLoader;
import org.createnet.raptor.dispatcher.Dispatcher;
import org.createnet.raptor.dispatcher.DispatcherConfiguration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
@Service
public class DispatcherService implements InitializingBean, DisposableBean {

    private Dispatcher dispatcher;

    public Dispatcher getDispatcher() {
        if (dispatcher == null) {
            dispatcher = new Dispatcher();
            dispatcher.initialize(getConfiguration());
        }
        return dispatcher;
    }
    
    protected DispatcherConfiguration getConfiguration() {
        return (DispatcherConfiguration) ConfigurationLoader
                .getConfiguration("dispatcher", DispatcherConfiguration.class);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        getDispatcher();
    }

    @Override
    public void destroy() throws Exception {
        getDispatcher().close();
    }

}
