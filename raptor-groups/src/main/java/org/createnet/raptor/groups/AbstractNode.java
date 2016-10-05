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
package org.createnet.raptor.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 * @param <T>
 */
public abstract class AbstractNode<T> implements Node<T> {

  protected Node parent = null;
  protected T item = null;
  final protected List<Node> children = new ArrayList();

  @Override
  public List<Node> getChildren() {
    return children;
  }

  @Override
  public boolean isRoot() {
    return getParent() == null;
  }

  @Override
  public Node getRoot() {
    Node node = this;
    while (!node.isRoot()) {
      node = node.getParent();
    }
    return node;
  }

  @Override
  public Node getParent() {
    return this.parent;
  }

  @Override
  public T get() {
    return item;
  }

  @Override
  public Node next() {

    if (isRoot()) {
      return null;
    }

    int currentIndex = getParent().getChildren().indexOf(this);
    int nextIndex = currentIndex + 1;

    if (nextIndex >= getParent().getChildren().size()) {
      return null;
    }

    return (Node) getParent().getChildren().get(nextIndex);
  }

  @Override
  public Node previous() {

    if (isRoot()) {
      return null;
    }

    int currentIndex = getParent().getChildren().indexOf(this);
    int prevIndex = currentIndex - 1;

    if (prevIndex < 0) {
      return null;
    }

    return (Node) getParent().getChildren().get(prevIndex);
  }

  @Override
  public List<Node> path() {

    List<Node> list = new ArrayList();

    if (isRoot()) {
      return list;
    }

    Node node = this;
    while (!node.isRoot()) {
      list.add(node);
      node = node.getParent();
    }
    
    Collections.reverse(list);
    return list;
  }
  
}
