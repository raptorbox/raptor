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
package org.createnet.raptor.client.api;

import org.createnet.raptor.client.AbstractClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.client.Raptor;
import org.createnet.raptor.client.event.MessageEventListener;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.indexer.query.impl.es.DataQuery;

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

    protected String getTopic(Stream stream) {
        String path = String.format(Client.Routes.SUBSCRIBE_STREAM, stream.getDevice().getId(), stream.name);
        return path;
    }

    /**
     * Subscribe a Stream for data updates
     *
     * @param stream The stream to listen data for
     * @param callback The callback to fire on data arrival
     */
    public void subscribe(Stream stream, StreamCallback callback) {
        getClient().subscribe(getTopic(stream), (MessageEventListener.Message message) -> {
            RecordSet record = RecordSet.fromJSON(message.content);
            callback.execute(stream, record);
        });
    }

    /**
     * Unsubscribe a Stream for data updates
     *
     * @param stream The stream to unsubscribe from
     */
    public void unsubscribe(Stream stream) {
        getClient().unsubscribe(getTopic(stream));
    }

    /**
     * Send stream data
     *
     * @param record the record to send
     */
    public void push(RecordSet record) {
        getClient().put(String.format(Client.Routes.PUSH, record.getStream().getDevice().id, record.getStream().name), record.toJsonNode());
    }

    /**
     * Send stream data
     *
     * @param objectId id of the object
     * @param streamId name of the stream
     * @param data data to send
     */
    public void push(String objectId, String streamId, RecordSet data) {
        getClient().put(String.format(Client.Routes.PUSH, objectId, streamId), data.toJsonNode());
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
        return ResultSet.fromJSON(stream, getClient().get(String.format(Client.Routes.PULL, stream.getDevice().id, stream.name) + qs));
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
        return getClient().get(String.format(Client.Routes.PULL, objectId, streamId) + qs);
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
     * Search for data in the stream
     *
     * @param stream the stream to search in
     * @param query the search query
     * @param offset results start from offset
     * @param limit limit the total size of result
     * @return the data resultset
     */
    public ResultSet search(Stream stream, DataQuery query, Integer offset, Integer limit) {
        String qs = buildQueryString(offset, limit);
        JsonNode results = getClient().post(
                String.format(Client.Routes.SEARCH_DATA, stream.getDevice().id, stream.name) + qs,
                query.toJSON()
        );
        return ResultSet.fromJSON(stream, results);
    }

}
