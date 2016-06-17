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
package org.createnet.raptor.dispatcher.router.impl;

import org.createnet.raptor.dispatcher.router.AbstractMessageRouter;
import org.createnet.raptor.dispatcher.router.MessageRouter.MessageRouterParams;
import org.createnet.raptor.plugin.PluginConfiguration;
import org.createnet.raptor.plugin.impl.BasePluginConfiguration;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class DefaultMessageRouter extends AbstractMessageRouter<
        DefaultMessageRouter.DefaultMessageRouterParams, 
        DefaultMessageRouterConfiguration
      > {

  protected MessageRouterParams params;
  protected String[] routes;
  protected String message;
  
  public static class DefaultMessageRouterParams implements MessageRouterParams {
    
    public enum Type {
      OBJECT, DATA, SUBSCRIPTION, ACTION
    }
    
    public String objectId;
    public String streamId;
    public String channelId;
    public String actionId;
    
  }
  
  @Override
  public void setup(MessageRouterParams params) {
    this.params = params;
  }

  @Override
  public String[] getRoutes() {
    return routes;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public PluginConfiguration<DefaultMessageRouterConfiguration> getPluginConfiguration() {
    return new BasePluginConfiguration("default", DefaultMessageRouterConfiguration.class);
  }

}
