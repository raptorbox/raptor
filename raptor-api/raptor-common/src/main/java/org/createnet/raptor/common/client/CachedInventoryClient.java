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
package org.createnet.raptor.common.client;

import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.api.InventoryClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class CachedInventoryClient extends InventoryClient {

    public CachedInventoryClient(Raptor container) {
        super(container);
    }

    @Override
    @Cacheable(cacheNames = "inventory", key = "#id")
    public Device load(String id) {
        return super.load(id);
    }
    
    @Override
    @CacheEvict(cacheNames = "inventory", key = "#device.id")
    public Device update(Device device) {
        return super.update(device);
    }
    
}
