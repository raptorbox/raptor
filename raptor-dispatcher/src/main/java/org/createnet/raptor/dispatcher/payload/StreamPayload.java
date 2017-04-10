/*
 * Copyright 2017 Luca Capra <luca.capra@fbk.eu>.
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
package org.createnet.raptor.dispatcher.payload;

import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class StreamPayload extends ObjectPayload {

    public String streamId;
    public JsonNode data;

    public StreamPayload(Stream stream, String op, JsonNode data) {
        super(stream.getDevice(), op);
        this.streamId = stream.name;
        this.data = data;
        this.type = MessageType.stream.name();
    }

}
