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
package org.createnet.raptor.client.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.client.RaptorClient;
import org.createnet.raptor.client.RaptorComponent;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.search.query.impl.es.DataQuery;

/**
 * Represent a service object data stream
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class StreamClient extends AbstractClient {

    /**
     * Send stream data
     *
     * @param record the record to send
     */
    public void push(RecordSet record) {
        getClient().put(RaptorComponent.format(RaptorClient.Routes.PUSH, record.getStream().getServiceObject().id, record.getStream().name), record.toJsonNode());
    }

    /**
     * Send stream data
     *
     * @param objectId id of the object
     * @param streamId name of the stream
     * @param data data to send
     */
    public void push(String objectId, String streamId, RecordSet data) {
        getClient().put(RaptorComponent.format(RaptorClient.Routes.PUSH, objectId, streamId), data.toJsonNode());
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
        return ResultSet.fromJSON(stream, getClient().get(RaptorComponent.format(RaptorClient.Routes.PULL, stream.getServiceObject().id, stream.name) + qs));
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
        return getClient().get(RaptorComponent.format(RaptorClient.Routes.PULL, objectId, streamId) + qs);
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
                RaptorComponent.format(RaptorClient.Routes.SEARCH_DATA, stream.getServiceObject().id, stream.name) + qs,
                query.toJSON()
        );
        return ResultSet.fromJSON(stream, results);
    }

}
