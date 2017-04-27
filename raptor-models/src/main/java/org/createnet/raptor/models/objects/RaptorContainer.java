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
package org.createnet.raptor.models.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.annotation.Transient;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public abstract class RaptorContainer implements RaptorComponent {

    abstract public void validate() throws ValidationException;

    abstract public void parse(String json) throws ParserException;

    static protected final ObjectMapper mapper = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return mapper;
    }

    @JsonIgnore
    @Transient
    protected RaptorComponent container;

    public RaptorComponent getContainer() {
        return container;
    }

    public void setContainer(RaptorComponent container) {
        this.container = container;
    }

}
