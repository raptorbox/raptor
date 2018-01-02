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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StringListQuery {

    protected List<String> in = new ArrayList();

    public StringListQuery in(List<String> vals) {
        vals.forEach((val) -> {
            if (!in.contains(val)) {
                in.add(val);
            }

        });
        return this;
    }

    public StringListQuery in(String val) {
        return in(Arrays.asList(val));
    }

    public List<String> getIn() {
        return in;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return (getIn() == null || getIn().isEmpty());
    }

}
