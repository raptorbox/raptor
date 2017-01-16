/*
 * Copyright 2017  FBK/CREATE-NET <http://create-net.fbk.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class ResultSet extends ArrayList<RecordSet> {

    @JsonIgnore
    protected Stream stream;

    @JsonIgnore
    private final Logger logger = LoggerFactory.getLogger(ResultSet.class);

    /**
     * Get the stream referencing this ResultSet records
     *
     * @return a stream instance
     */
    public Stream getStream() {
        return stream;
    }

    /**
     * Set the stream referencing this ResultSet records
     *
     * @param stream a stream instance
     */
    public void setStream(Stream stream) {
        this.stream = stream;
    }

    public ResultSet() {
    }

    public ResultSet(Stream stream) {
        setStream(stream);
    }

    public ResultSet(Stream stream, String jsonString) {
        this(stream);
        if (jsonString != null) {
            parse(jsonString);
        }
    }

    public ResultSet(Stream stream, JsonNode jsonString) {
        this(stream);
        if (jsonString != null) {
            parse(jsonString);
        }
    }
    
    private void parse(String jsonString) {

        ObjectMapper mapper = ServiceObject.getMapper();
        JsonNode json;

        try {
            json = mapper.readTree(jsonString);
        } catch (IOException ex) {
            logger.error("Error parsing: {}", jsonString, ex);
            return;
        }

        parse(json);
    }

    private void parse(JsonNode json) {
        if (json.isArray()) {
            for (JsonNode row : json) {
                this.add(new RecordSet(this.getStream(), row));
            }
        }

    }
    
    /**
     * Add a record from a raw string
     * @param raw the string with json to add
     * @return operation result
     */
    public boolean add(String raw) {
        RecordSet recordset = new RecordSet(stream, raw);
        return this.add(recordset);
    }

    /**
     * Add a record from a raw JsonNode
     * @param raw the string with JsonNode to add
     * @return operation result
     */
    public boolean add(JsonNode raw) {
        RecordSet recordset = new RecordSet(stream, raw);
        return this.add(recordset);
    }
    
    /**
     * Return the string JSON ResultSet
     * @return string JSON ResultSet
     */
    public String toJson() {
        return toJsonNode().toString();
    }

    /**
     * Return the JsonNode representation of the ResultSet
     * @return JsonNode representation of the ResultSet
     */
    public ArrayNode toJsonNode() {
        ObjectMapper mapper = ServiceObject.getMapper();
        ArrayNode list = mapper.createArrayNode();
        this.forEach((record) -> {
            list.add(record.toJsonNode());
        });
        return list;
    }

    @Override
    public String toString() {
        return toJson();
    }

    public static ResultSet fromJSON(Stream stream, JsonNode raw) {
        return new ResultSet(stream, raw);
    }

    public static ResultSet fromJSON(Stream stream, String raw) {
        return new ResultSet(stream, raw);
    }

}
