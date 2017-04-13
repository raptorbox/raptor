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
package org.createnet.raptor.models.data.types;

import java.util.LinkedHashMap;
import java.util.Map;
import org.createnet.raptor.models.data.Record;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class TypesManager {

    static private Map<String, Record> types = new LinkedHashMap();

    public static class BadTypeException extends RuntimeException {

        public BadTypeException(Throwable cause) {
            super(cause);
        }

    }

    static public void addType(String name, Class<? extends Record> type) {
        try {
            getTypes().put(name, type.newInstance());
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new BadTypeException(ex);
        }
    }

    static public Map<String, Record> getTypes() {

        // push defaults
        if (types.isEmpty()) {

            Record instance;

            // Number
            instance = new NumberRecord();
            types.put(instance.getType(), instance);

            // Boolean
            instance = new BooleanRecord();
            types.put(instance.getType(), instance);

            // Boolean
            instance = new GeoPointRecord();
            types.put(instance.getType(), instance);

            // String
            instance = new StringRecord();
            types.put(instance.getType(), instance);

        }

        return types;
    }

}
