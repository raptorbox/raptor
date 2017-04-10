/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.models.events;

import java.util.List;
import org.createnet.raptor.models.objects.Device;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class DeviceEvent implements IEvent {

    private Device object = null;
    private List<Device> list = null;

    public DeviceEvent(Device object) {
        this.object = object;
    }
    
    public DeviceEvent(List<Device> objects) {
        this.list = objects;
    }

    public Device getObject() {
        return object;
    }
    
    public List<Device> getList() {
        return list;
    }

}
