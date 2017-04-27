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
package org.createnet.raptor.models.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import org.createnet.raptor.models.data.Record;
import org.createnet.raptor.models.data.types.TypesManager;
import org.springframework.data.annotation.Transient;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public abstract class DeviceContainer extends RaptorContainer {

    @JsonIgnore
    @Transient
    protected Device device;

    protected Map<String, Record> getTypes() {
        return TypesManager.getTypes();
    }

    @Override
    public RaptorComponent getContainer() {
        if (getDevice() == null) {
            return null;
        }
        return getDevice().getContainer();
    }

    public void setDevice(Device _device) {
        this.device = _device;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    abstract public void validate() throws ValidationException;

    @Override
    abstract public void parse(String json) throws ParserException;

}
