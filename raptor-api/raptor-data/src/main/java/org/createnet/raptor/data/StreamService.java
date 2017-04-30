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
package org.createnet.raptor.data;

import java.util.List;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class StreamService {

    @Autowired
    private StreamRepository repository;

    public void save(RecordSet record) {
        repository.save(record);
    }

    public List<RecordSet> list(Stream stream, Pageable page) {
        return repository.findByObjectIdAndStreamId(stream.getDevice().getId(), stream.name, page);
    }

    public RecordSet lastUpdate(Stream stream) {

        Page<RecordSet> records = repository.findAll(new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "timestamp")));

        if (records.getTotalElements() == 0) {
            return null;
        }

        return records.getContent().get(0);

    }

    public void deleteAll(Stream stream) {
        repository.deleteByObjectIdAndStreamId(stream.getDevice().getId(), stream.name);
    }

}
