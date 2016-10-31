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
package org.createnet.raptor.http.service;

import javax.inject.Singleton;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Singleton
@Service
public class RaptorApplicationEventListener implements ApplicationEventListener {

    protected static final Logger logger = LoggerFactory.getLogger(RaptorApplicationEventListener.class);

    @Override
    public void onEvent(ApplicationEvent applicationEvent) {
        switch (applicationEvent.getType()) {
            case INITIALIZATION_FINISHED:
                logger.debug("Raptor HTTP API app started.");
                break;
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new RaptorRequestEventListener();
    }

    public static class RaptorRequestEventListener implements RequestEventListener {

        private volatile long methodStartTime;

        @Override
        public void onEvent(RequestEvent requestEvent) {
            switch (requestEvent.getType()) {
                case RESOURCE_METHOD_START:
                    methodStartTime = System.currentTimeMillis();
                    break;
                case RESOURCE_METHOD_FINISHED:
                    long methodExecution = System.currentTimeMillis() - methodStartTime;
                    final String methodName = requestEvent.getUriInfo().getMatchedResourceMethod().getInvocable().getHandlingMethod().getName();
                    logger.debug("Method '{}' executed. Processing time: {} ms", methodName, methodExecution);
                    break;
            }
        }
    }
}
