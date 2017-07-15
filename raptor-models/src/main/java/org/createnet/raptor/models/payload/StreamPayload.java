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
package org.createnet.raptor.models.payload;

import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class StreamPayload extends DevicePayload {

    public String streamId;
    public RecordSet record;

    public StreamPayload() {
    }
    
    public StreamPayload(Stream stream, Permissions op, RecordSet record) {
        super(stream.getDevice(), op);
        this.streamId = stream.name();
        this.record = record;
        this.type = MessageType.stream;
    }

}
