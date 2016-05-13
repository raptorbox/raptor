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
package org.createnet.raptor.models.objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import org.createnet.raptor.models.data.Record;
import org.createnet.raptor.models.data.types.TypesManager;
import org.createnet.raptor.models.events.ServiceObjectEventListener;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
abstract class ServiceObjectContainer extends RaptorContainer {

    @JsonIgnore
    protected ServiceObjectEventListener listener;
    
    @JsonIgnore
    protected ServiceObject serviceObject;
    
    protected Map<String, Record> getTypes() {
      return TypesManager.getTypes();
    }
    
    @Override
    public RaptorComponent getContainer() {
        if(getServiceObject() == null) return null;
        return getServiceObject().getContainer();
    }
    
    public void setServiceObject(ServiceObject _serviceObject) {
        this.serviceObject = _serviceObject;
//        if(_serviceObject != null) {
//          this.setContainer(_serviceObject.getContainer());
//        }
    }

    public ServiceObject getServiceObject() {
        return serviceObject;
    }

    @Override
    public ServiceObjectEventListener getListener() {
        return listener;
    }

    public void setListener(ServiceObjectEventListener listener) {
        this.listener = listener;
    }    
    
    @Override
    abstract public void validate() throws ValidationException;

    @Override
    abstract public void parse(String json) throws ParserException;
    
}
