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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeName("number")
public class NumberQuery implements IQuery {
    
    private final Number[] between = new Number[2];
    
    public NumberQuery between(Number min, Number max) {
        this.between[0] = min;
        this.between[1] = max;
        return this;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return (
            getBetween().length == 0
        );
    }

    public Number[] getBetween() {
        return between;
    }
    
}
