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
package org.createnet.raptor.models.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import org.createnet.raptor.models.query.deserializer.TreeQueryDeserializer;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(using = TreeQueryDeserializer.class)
public class TreeQuery extends BaseQuery {

    public final TextQuery id = new TextQuery();
    public final TextQuery name = new TextQuery();
    public final TextQuery domain = new TextQuery();
    public final TextQuery description = new TextQuery();
    public final TextQuery parentId = new TextQuery();
    public final MapQuery properties = new MapQuery();
    
    public TreeQuery() {
    }

    public TreeQuery(String userId) {
        super.userId = userId;
    }
    
    public static TreeQuery queryByDeviceId(String... ids) {
        TreeQuery q = new TreeQuery();
        q.id.in(ids);
        return q;
    }
    
    public static TreeQuery queryByDomainId(String... ids) {
        TreeQuery q = new TreeQuery();
        q.domain.in(ids);
        return q;
    }
    
    public static TreeQuery queryByDeviceId(List<String> deviceId) {
        TreeQuery q = new TreeQuery();
        q.id.in(deviceId.toArray(new String[deviceId.size()]));
        return q;
    }
    
    public static TreeQuery queryByDomainId(List<String> ids) {
        TreeQuery q = new TreeQuery();
        q.domain.in(ids.toArray(new String[ids.size()]));
        return q;
    }
    
}
