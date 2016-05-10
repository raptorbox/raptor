/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class AbstractConnection implements Storage.Connection {
  
  protected String id;
  
  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
}
