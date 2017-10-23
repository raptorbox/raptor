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

import org.createnet.raptor.models.auth.User;
import org.springframework.security.acls.model.ObjectIdentity;

/**
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AclSubjectImpl extends AbstractAclSubject {

    protected final UserSid sid;
    protected final AclSubject subj;

    public AclSubjectImpl(AclSubject subj, User user) {
        this.sid = new UserSid(user);
        this.subj = subj;
    }

    public AclSubjectImpl(AclSubject subj) {
        this.sid = subj.getSid();
        this.subj = subj;
    }

    @Override
    public User getUser() {
        if(getSid() != null) {
            return getSid().getUser();
        }
        return subj.getSid().getUser();
    }

    @Override
    public Long getId() {
        return (Long) getObjectIdentity().getIdentifier();
    }

    @Override
    public AclSubject getParent() {
        return subj.getParent();
    }

    @Override
    public ObjectIdentity getObjectIdentity() {
        return subj.getObjectIdentity();
    }

    @Override
    public UserSid getSid() {
        return sid;
    }

}
