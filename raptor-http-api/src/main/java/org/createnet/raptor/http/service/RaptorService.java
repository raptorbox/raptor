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
package org.createnet.raptor.http.service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public interface RaptorService {

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
     * Reset internal state of a service, causing a reinitialization
     */
    public void reset() throws ServiceException;

}
