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
package org.createnet.raptor.models.data;

import org.createnet.raptor.models.objects.RaptorComponent;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class StringRecord extends Record<String> {
    
    protected String value;

    @Override
    public String getType() {
        return "string";
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) throws RaptorComponent.ParserException {
        this.value = parseValue(value);
    }
    
    @Override
    public String parseValue(Object value) throws RaptorComponent.ParserException {
      try {
        return (String)value;
      } catch (Exception e) {
        throw new RaptorComponent.ParserException(e);
      }
    }
    
}
