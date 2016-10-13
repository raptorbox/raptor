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
package org.createnet.raptor.models.data.types;

import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.models.data.Record;
import org.createnet.raptor.models.objects.RaptorComponent;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class BooleanRecord extends Record<Boolean> {

    protected boolean value;

    @Override
    public String getType() {
        return "boolean";
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = parseValue(value);
    }

    @Override
    public Boolean parseValue(Object value) {
        try {

            if (value instanceof Boolean) {
                return (Boolean) value;
            }

            if (value instanceof JsonNode) {
                JsonNode node = (JsonNode) value;
                if (node.isBoolean()) {
                    return node.asBoolean();
                }
            }

            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }

            return (Boolean) value;
        } catch (Exception e) {
            throw new RaptorComponent.ParserException(e);
        }
    }

    @Override
    public Class<Boolean> getClassType() {
        return Boolean.class;
    }

}
