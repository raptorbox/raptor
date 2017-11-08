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
package org.createnet.raptor.models.acl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.createnet.raptor.models.auth.User;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
abstract public class AbstractAclSubject implements AclSubject {
    
    protected ObjectIdentity oi;
    protected UserSid sid;
    protected AclSubject parent;

    abstract public User getUser();
    abstract public Long getId();
    
    @JsonIgnore
    @Override
    public UserSid getSid() {
        if (sid == null) {
            User u = getUser();
            assert u != null;
            sid = new UserSid(u);
        }
        return sid;
    }

    @JsonIgnore
    @Override
    public ObjectIdentity getObjectIdentity() {
        if (oi == null) {
            assert getId() != null;
            oi = new ObjectIdentityImpl(this);
        }
        return oi;
    }

    @JsonIgnore
    @Override
    public AclSubject getParent() {
        return parent;
    }

    public void setParent(AclSubject parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        String o = "";
        if (getId() != null) {
            o = getObjectIdentity().toString();
        }
        String u = "";
        if (sid != null) {
            u = getSid().getUser().getId();
        }
        String p = "";
        if (parent != null) {
            p = parent.toString();
        }
        
        return String.format("AclSubject[oi=%s, user=%s parent=%s]", o, u, p);
    }
    
}
