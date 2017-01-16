/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.models.events;

import org.createnet.raptor.models.objects.Action;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class ActionEvent implements IEvent {

    private final Action actuation;
    private String status;

    public ActionEvent(Action actuation, String status) {
        this.actuation = actuation;
        this.status = status;
    }

    public Action getAction() {
        return actuation;
    }

    public String getStatus() {
        return status;
    }

}
