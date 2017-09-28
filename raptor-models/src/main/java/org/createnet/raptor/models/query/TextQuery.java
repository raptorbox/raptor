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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeName("text")
public class TextQuery implements IQuery {
    
    private String startWith;
    private String endWith;
    private String contains;
    private String equals;
    private final List<String> in = new ArrayList<String>();

    public TextQuery startWith(String startWith) {
        this.startWith = startWith;
        return this;
    }

    public TextQuery endWith(String endWith) {
        this.endWith = endWith;
        return this;
    }

    public TextQuery contains(String contains) {
        this.contains = contains;
        return this;
    }

    public TextQuery match(String equals) {
        this.equals = equals;
        return this;
    }

    public TextQuery in(String... values) {
        this.in.addAll(Arrays.asList(values));
        return this;
    }
    
    public TextQuery in(String value) {
        this.in.add(value);
        return this;
    }
    
    @JsonIgnore
    public boolean isEmpty() {
        return (
            getStartWith() == null &&
            getEndWith() == null &&
            getContains() == null &&
            getEquals() == null &&
            getIn().isEmpty()
        );
    }
    
    public String getStartWith() {
        return this.startWith;
    }

    public String getEndWith() {
        return this.endWith;
    }

    public String getContains() {
        return this.contains;
    }

    public String getEquals() {
        return this.equals;
    }
    
    public String getMatch() {
        return this.equals;
    }
    
    public List<String> getIn() {
        return this.in;
    }

}
