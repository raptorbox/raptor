/*
 * Copyright 2017 FBK/CREATE-NET.
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

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.type.DataEvent;
import org.createnet.raptor.indexer.query.Query;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.indexer.query.impl.es.DataQuery;
import org.createnet.raptor.service.AbstractRaptorService;
import org.createnet.raptor.service.exception.StreamNotFoundException;
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
 * @author Luca Capra <luca.capra@fbk.eu>
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
    protected DispatcherService dispatcher;

    @Inject
    protected TreeService tree;

    private final static Logger logger = LoggerFactory.getLogger(StreamManagerService.class);

    /**
     * Load a stream based on object id and its name
     *
     * @param objectId id of the objectId
     * @param streamId name of the stream
     * @return a Stream instance
     */
    public Stream load(String objectId, String streamId) {

        // just an effort to ensure arguments are not flipped
        assert objectId.length() == 36;

        Device obj = objectManager.load(objectId);
        Stream stream = obj.streams.getOrDefault(streamId, null);

        if (stream == null) {
            throw new StreamNotFoundException("Stream " + streamId + " not found");
        }

        return stream;

    }

    /**
     * Fetch the last record inserted in a stream
     *
     * @param stream the stream
     * @return the last update
     */
    public RecordSet lastUpdate(Stream stream) {

        Device obj = stream.getDevice();

        assert obj != null;

        if (!obj.settings.storeEnabled()) {
            return null;
        }

        RecordSet data = indexer.fetchLastUpdate(stream);

        logger.debug("Fetched lastUpdate record for stream {} in object {}", stream.name, obj.id);

        return data;
    }

    public void delete(Stream stream) {

        Device obj = stream.getDevice();
        assert obj != null;

        if (!obj.settings.storeEnabled()) {
            return;
        }

        storage.deleteData(stream);
        indexer.deleteData(stream);

        logger.debug("Delete all records for stream {} in object {}", stream.name, obj.id);
    }

    public void push(Stream stream, RecordSet record, String userId) {

        Device obj = stream.getDevice();
        assert obj != null;

        // set the stream, enforcing channels constrain on serialization
        // this avoid records that do not comply with the stored model
        record.userId = userId;
        record.setStream(stream);
        record.validate();

        if (obj.settings.storeEnabled()) {

            logger.debug("Storing data for {} on {}", obj.id, stream.name);

            // save data
            storage.saveData(stream, record);
//            profiler.log("Saved record");

            // index data (with objectId and stream props)
            try {
                indexer.saveData(record);
            } catch (Exception ex) {
                logger.error("Failed to index record for {}: {}", obj.id, ex.getMessage(), ex);
                storage.deleteData(stream, record);
                throw new InternalServerErrorException();
            }

        } else {
            logger.debug("Skipped data storage for {}", obj.id);
        }

        emitter.trigger(Event.EventName.push, new DataEvent(stream, record));

        logger.debug("Received record for stream {} in object {}", stream.name, obj.id);

    }

    /**
     * Search for data
     *
     * @param stream the stream to search for
     * @param query query to search for
     * @param offset offset to start from results
     * @param limit limit total result per request
     * @return a set of records
     */
    public ResultSet search(Stream stream, DataQuery query, Integer offset, Integer limit) {

        Device obj = stream.getDevice();
        assert obj != null;

        if (!obj.settings.storeEnabled()) {
            return null;
        }
                
        ResultSet results = indexer.searchData(stream, query, limit, offset);

        logger.debug("Search data for stream {} in object {} has {} results", stream.name, obj.id, results.size());

        return results;
    }

    /**
     * Search for data
     *
     * @param stream the stream to search for
     * @param query query to search for
     * @return a set of records
     */
    public ResultSet search(Stream stream, DataQuery query) {
        return search(stream, query, 0, null);
    }

    public ResultSet fetch(Stream stream, Integer offset, Integer limit) {
        
        Device obj = stream.getDevice();
        assert obj != null;
        
        if (!obj.settings.storeEnabled()) {
            return null;
        }

        ResultSet result = indexer.fetchData(stream, limit, offset);

        logger.debug("Fetched {} records for stream {} in object {}", result.size(), stream.name, obj.id);

        return result;
    }

    
    
}
