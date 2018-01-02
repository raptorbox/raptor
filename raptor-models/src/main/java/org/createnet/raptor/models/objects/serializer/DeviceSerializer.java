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
import org.createnet.raptor.models.objects.Device;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class DeviceSerializer extends JsonSerializer<Device> {

    @Override
    public void serialize(Device t, JsonGenerator jg, SerializerProvider sp) throws IOException {

        jg.writeStartObject();

        if (t.id() != null) {
            jg.writeStringField("id", t.id());
        }

        if (t.userId() != null) {
            jg.writeStringField("userId", t.userId());
        }

        if (t.name() != null) {
            jg.writeStringField("name", t.name());
        }

        if (t.description() != null) {
            jg.writeStringField("description", t.description());
        }
        
        if (t.domain() != null) {
            jg.writeStringField("domain", t.domain());
        }

        jg.writeObjectField("streams", t.streams());
        jg.writeObjectField("actions", t.actions());

        jg.writeObjectField("properties", t.properties());
        jg.writeObjectField("settings", t.settings());

        jg.writeEndObject();

    }

}
