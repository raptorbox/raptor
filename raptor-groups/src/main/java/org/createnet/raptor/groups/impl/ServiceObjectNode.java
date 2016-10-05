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
package org.createnet.raptor.groups.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.groups.AbstractNode;
import org.createnet.raptor.groups.Node;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ServiceObjectNode extends AbstractNode<ServiceObject>{
  
  final protected ServiceObject item;
  
  public ServiceObjectNode(ServiceObject obj) {
    this.item = obj;
  }

  @Override
  public Node getParent() {

    if(item.getParent() == null )  {
      return null;
    }
    
    return new ServiceObjectNode(item.getParent());
  }

  @Override
  public List<Node> getChildren() {
    return item.getChildren().stream().map((ServiceObject o)-> { return new ServiceObjectNode(o); }).collect(Collectors.toList());
  }
  
}
