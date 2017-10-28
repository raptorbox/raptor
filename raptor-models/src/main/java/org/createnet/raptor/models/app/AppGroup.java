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
package org.createnet.raptor.models.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.createnet.raptor.models.auth.StaticGroup;
import org.createnet.raptor.models.auth.Group;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class AppGroup {

    public static AppGroup from(Group group) {
        AppGroup g = new AppGroup(group);
        return g;
    }

    protected String name;
    protected final List<String> permissions = new ArrayList();

    public AppGroup() {
    }

    public AppGroup(String name) {
        this.name = name;
    }

    public AppGroup(StaticGroup group) {
        this.name = group.name();
    }

    public AppGroup(String name, List<String> permissions) {
        this(name);
        this.permissions.addAll(permissions);
    }

    public AppGroup(Group group) {
        this.name = group.getName();
        this.permissions.addAll(group.getPermissions().stream().map((p) -> p.getName()).collect(Collectors.toList()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        if (permissions == null) {
            return;
        }
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    public void addPermissions(List<String> permissions) {
        if (permissions == null) {
            return;
        }
        permissions.forEach((p) -> {
            if (!getPermissions().contains(p)) {
                getPermissions().add(p);
            }
        });
    }

    public void addPermission(String permission) {
        if (permission == null) {
            return;
        }
        addPermissions(Arrays.asList(permission));
    }

    public void removePermission(String permission) {
        if (permission == null) {
            return;
        }
        if (getPermissions().contains(permission)) {
            getPermissions().remove(permission);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppGroup other = (AppGroup) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
