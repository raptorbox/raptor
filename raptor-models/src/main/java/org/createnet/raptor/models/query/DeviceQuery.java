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
import java.util.List;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeviceQuery extends BaseQuery {

    public final TextQuery id = new TextQuery();
    public final TextQuery name = new TextQuery();
    public final TextQuery description = new TextQuery();
    public final MapQuery properties = new MapQuery();
    
    public DeviceQuery() {
    }

    public DeviceQuery(String userId) {
        this.userId = userId;
    }
    
    public static DeviceQuery queryByDeviceId(String... deviceId) {
        DeviceQuery q = new DeviceQuery();
        q.id.in(deviceId);
        return q;
    }
    
    public static DeviceQuery queryByDeviceId(List<String> deviceId) {
        DeviceQuery q = new DeviceQuery();
        q.id.in(deviceId.toArray(new String[deviceId.size()]));
        return q;
    }
    
}
