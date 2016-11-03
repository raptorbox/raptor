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

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * A service exposes an API component set of features
 * 
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface RaptorService {
    
    /**
     * Lookup for the class instance to inject registered services
     * @param injectMe the class instance to inject with service
     */
    public static void inject(Object injectMe) {

        ServiceLocatorFactory locatorFactory = ServiceLocatorFactory.getInstance();
        ServiceLocator serviceLocator = locatorFactory.create("ObjectApiTest");
        ServiceLocatorUtilities.bind(serviceLocator, new ServiceBinder());

        serviceLocator.inject(injectMe);
    }
    
    public class ServiceException extends RuntimeException {

        public ServiceException(String message) {
            super(message);
        }

        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public ServiceException(Exception e) {
            super(e);
        }

    }

    /**
     * Warm up the service
     */
    public void initialize() throws ServiceException;

    /**
     * Teardown the service
     */
    public void shutdown() throws ServiceException;
    
    /**
     * Reset the internal state of a service and reinitialization
     */
    public void reset() throws ServiceException;

}
