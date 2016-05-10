/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.models.events;

import java.util.List;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
public class ServiceObjectEvent implements IEvent {

    private ServiceObject object = null;
    private List<ServiceObject> list = null;

    public ServiceObjectEvent(ServiceObject object) {
        this.object = object;
    }
    
    public ServiceObjectEvent(List<ServiceObject> objects) {
        this.list = objects;
    }

    public ServiceObject getObject() {
        return object;
    }
    
    public List<ServiceObject> getList() {
        return list;
    }

}
