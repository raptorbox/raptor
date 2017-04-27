/*
 * Copyright 2017 FBK/CREATE-NET
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class DeviceNode {

    Device current;
    List<DeviceNode> children = new ArrayList();
    DeviceNode parent;

    public DeviceNode(Device current) {
        this(current, null);
    }

    public DeviceNode(Device current, DeviceNode parent) {
        this.current = current;
        this.parent = parent;
    }

    public Device getCurrent() {
        return current;
    }

    public List<DeviceNode> getChildren() {
        return children;
    }

    public void addChildren(List<DeviceNode> nodes) {
        nodes.stream().forEach(node -> this.addChild(node));
    }

    public void addChild(DeviceNode node) {

        node.getCurrent().parentId = this.getCurrent().id;
        node.setParent(this);

        if (this.children.contains(node)) {
            return;
        }

        children.add(node);
    }

    public void removeChildren(List<DeviceNode> nodes) {
        nodes.stream().forEach(node -> this.removeChild(node));
    }

    public void removeChild(DeviceNode node) {

        if (!this.children.contains(node)) {
            return;
        }

        node.getCurrent().parentId = null;
        node.setParent(null);

        children.remove(node);
    }

    public DeviceNode getParent() {
        return parent;
    }

    public void setCurrent(Device current) {
        this.current = current;
    }

    public void setParent(DeviceNode parent) {
        this.parent = parent;
    }

    public DeviceNode getRoot() {
        return (getParent() == null) ? this : getRoot();
    }

    public List<DeviceNode> tree() {
        DeviceNode node = this;
        List<DeviceNode> parts = new ArrayList();
        while (node.getParent() != null) {
            parts.add(node);
            node = node.getParent();
        }
        parts.add(node);
        Collections.reverse(parts);
        return parts;
    }

    public List<Device> objects() {
        DeviceNode node = this;
        final List<Device> parts = new ArrayList();
        while (node.getParent() != null) {
            parts.add(node.getCurrent());
            node = node.getParent();
        }
        parts.add(node.getCurrent());
        Collections.reverse(parts);
        return parts;
    }

    public String path() {
        return String.join("/", tree().stream().map(n -> n.getCurrent().id).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return getCurrent().parentId + "." + getCurrent().id + " [" + path() + "]";
    }

    public final Optional<DeviceNode> getChild(String id) {
        final Optional<DeviceNode> o = getChildren().stream().filter(n -> n.getCurrent().id.equals(id)).findFirst();
        return o;
    }

}
