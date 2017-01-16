/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.models.events;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public interface ServiceObjectEventListener extends IEventListener {

    public void onList(ServiceObjectEvent serviceObjectEvent);
}
