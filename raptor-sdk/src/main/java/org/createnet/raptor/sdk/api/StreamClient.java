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
package org.createnet.raptor.sdk.api;

import org.createnet.raptor.sdk.Routes;
import org.createnet.raptor.sdk.AbstractClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.events.callback.DataCallback;
import org.createnet.raptor.sdk.events.callback.StreamEventCallback;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.payload.DataPayload;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.sdk.RequestOptions;

/**
 * Represent a Device data stream
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class StreamClient extends AbstractClient {

    public StreamClient(Raptor container) {
        super(container);
    }

    public interface StreamCallback {

        /**
         * Run when a stream receive data
         *
         * @param stream The stream receiving update
         * @param record The record sent over stream
         */
        public void execute(Stream stream, RecordSet record);
    }

    /**
     * Subscribe to a data stream
     *
     * @param stream
     * @param ev
     */
    public void subscribe(Stream stream, StreamEventCallback ev) {
        getEmitter().subscribe(stream, ev);
    }

    /**
     * Subscribe to a data stream
     *
     * @param stream
     * @param ev
     */
    public void subscribe(Stream stream, DataCallback ev) {
        subscribe(stream, new StreamEventCallback() {
            @Override
            public void trigger(DispatcherPayload payload) {
                DataPayload dpayload = (DataPayload) payload;
                RecordSet record = RecordSet.fromJSON(dpayload.toString());
                ev.callback(stream, record);
            }
        });
    }

    /**
     * Send stream data
     *
     * @param record the record to send
     */
    public void push(RecordSet record) {
        getClient().put(String.format(Routes.PUSH, record.getStream().getDevice().id(), record.getStream().name), record.toJsonNode(), RequestOptions.retriable().waitFor(300).maxRetries(5));
    }

    /**
     * Send stream data
     *
     * @param s
     * @param record the record to send
     */
    public void push(Stream s, RecordSet record) {
        getClient().put(String.format(Routes.PUSH, s.getDevice().id(), s.name), record.toJsonNode());
    }

    /**
     * Send stream data
     *
     * @param objectId id of the object
     * @param streamId name of the stream
     * @param data data to send
     */
    public void push(String objectId, String streamId, RecordSet data) {
        getClient().put(String.format(Routes.PUSH, objectId, streamId), data.toJsonNode());
    }

    /**
     * Retrieve data from a stream
     *
     * @param stream the stream to read from
     * @return the data resultset
     */
    public ResultSet pull(Stream stream) {
        return pull(stream, 0, null);
    }

    /**
     * Retrieve data from a stream
     *
     * @param stream the stream to read from
     * @param offset results start from offset
     * @param limit limit the total size of result
     * @return the data resultset
     */
    public ResultSet pull(Stream stream, Integer offset, Integer limit) {
        String qs = buildQueryString(offset, limit);
        return ResultSet.fromJSON(stream, getClient().get(String.format(Routes.PULL, stream.getDevice().id(), stream.name) + qs));
    }

    /**
     * Retrieve data from a stream
     *
     * @param objectId id of the object
     * @param streamId name of the stream
     * @param offset results start from offset
     * @param limit limit the total size of result
     * @return the data resultset
     */
    public JsonNode pull(String objectId, String streamId, Integer offset, Integer limit) {
        String qs = buildQueryString(offset, limit);
        return getClient().get(String.format(Routes.PULL, objectId, streamId) + qs);
    }

    /**
     * Retrieve data from a stream
     *
     * @param objectId id of the object
     * @param streamId name of the stream
     * @return the data resultset
     */
    public JsonNode pull(String objectId, String streamId) {
        return pull(objectId, streamId, null, null);
    }

    /**
     * Fetch the last record stored in the stream
     *
     * @param stream
     * @return
     */
    public RecordSet lastUpdate(Stream stream) {
        JsonNode result = getClient().get(String.format(Routes.LAST_UPDATE, stream.getDevice().id(), stream.name));
        if (result == null) {
            return null;
        }
        return RecordSet.fromJSON(result);
    }

    /**
     * Search for data in the stream
     *
     * @param stream the stream to search in
     * @param query the search query
     * @return 
     */
    public ResultSet search(Stream stream, DataQuery query) {
        JsonNode results = getClient().post(
                String.format(Routes.SEARCH_DATA, stream.getDevice().id(), stream.name),
                query.toJSON()
        );
        return ResultSet.fromJSON(stream, results);
    }

    /**
     * Drop all data stored in a stream
     *
     * @param stream
     */
    public void delete(Stream stream) {
        getClient().delete(
                String.format(Routes.STREAM, stream.getDevice().id(), stream.name)
        );
    }

}
