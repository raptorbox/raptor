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
package org.createnet.raptor.inventory;

import com.querydsl.core.types.Predicate;
import java.util.List;
import org.createnet.raptor.models.objects.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class DeviceService {
    
    @Autowired
    private DeviceRepository repository;
    
    public void save(Device device) {
        repository.save(device);
    }
    
    public Device get(String deviceId) {
        return repository.findOne(deviceId);
    }
    
    public List<Device> list(String userId) {
        return repository.findByUserId(userId);
    }

    public void delete(String devId) {
        repository.delete(devId);
    }

    public void delete(Device dev) {
        delete(dev.id());
    }

    public Page<Device> search(Predicate predicate, Pageable pageable) {
        return repository.findAll(predicate, pageable);
    }

    public List<Device> search(Predicate predicate) {
        return repository.findAll(predicate);
    }
        
}
