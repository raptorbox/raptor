/*
 * Copyright 2016 CREATE-NET.
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
package org.createnet.raptor.service.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.ObjectEvent;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.search.Indexer;
import org.createnet.raptor.search.query.impl.es.ObjectQuery;
import org.createnet.raptor.service.AbstractRaptorService;
import org.createnet.raptor.service.exception.ObjectNotFoundException;
import org.createnet.raptor.service.exception.StreamNotFoundException;
import org.createnet.raptor.service.tools.AuthService;
import org.createnet.raptor.service.tools.DispatcherService;
import org.createnet.raptor.service.tools.EventEmitterService;
import org.createnet.raptor.service.tools.IndexerService;
import org.createnet.raptor.service.tools.StorageService;
import org.createnet.raptor.service.tools.TreeService;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API to manage Object definitions
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
@Service
public class StreamManagerService extends AbstractRaptorService {

    @Inject
    protected ObjectManagerService objectManager;
    
    @Inject
    protected EventEmitterService emitter;

    @Inject
    protected StorageService storage;

    @Inject
    protected IndexerService indexer;

    @Inject
    protected AuthService auth;

    @Inject
    protected DispatcherService dispatcher;

    @Inject
    protected TreeService tree;

    private final static Logger logger = LoggerFactory.getLogger(StreamManagerService.class);

    protected Stream loadStream(String objectId, String streamId) {
        
        ServiceObject obj = objectManager.load(objectId);
        Stream stream = obj.streams.getOrDefault(streamId, null);

        if (stream == null) {
            throw new StreamNotFoundException("Stream " + streamId + " not found");
        }

        return stream;

    }
    
    /**
     * Return the list of streams available
     * @param id
     * @return 
     */
    public Collection<Stream> loadStreams(String id) {

        ServiceObject obj = objectManager.loadById(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot load stream list");
        }

        logger.debug("Load streams for object {}", obj.id);

        return obj.streams.values();
    }

}
