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
package org.createnet.raptor.models.objects.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class StreamSerializer extends JsonSerializer<Stream> {

    @Override
    public void serialize(Stream t, JsonGenerator jg, SerializerProvider sp) throws IOException {

        jg.writeStartObject();
        
        jg.writeStringField("name", t.name());
        
        if (t.type() != null) {
            jg.writeStringField("type", t.type().name());
        }
        
        jg.writeStringField("description", t.description());

        if(!t.channels().isEmpty()) {
            jg.writeObjectField("channels", t.channels());
        }

        jg.writeEndObject();

    }
    
}
