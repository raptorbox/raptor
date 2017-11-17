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
package org.createnet.raptor.models.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class SortQuery {
    
    public enum Direction { ASC, DESC }
    
    private Direction direction = Direction.DESC;
    final List<String> fields = new ArrayList();

    public SortQuery() {
//        this.fields.add("createdAt");
    }
    
    public SortQuery addField(String... fields) {
        this.fields.addAll(Arrays.asList(fields));
        return this;
    }
    
    public SortQuery direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    public String getDirection() {
        return direction.name();
    }

    public List<String> getFields() {
        return fields;
    }
    
}
