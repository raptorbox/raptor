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
package org.createnet.raptor.api.common.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Iterator;
import org.createnet.raptor.models.data.QRecordSet;
import org.createnet.raptor.models.objects.QDevice;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.models.query.MapQuery;
import org.createnet.raptor.models.query.TextQuery;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DataQueryBuilder extends BaseQueryBuilder {

    private final DataQuery query;

    public DataQueryBuilder(DataQuery query) {
        this.query = query;
    }

    public Pageable getPaging() {
        return getPaging(query);
    }

    public Predicate getPredicate() {

        QRecordSet record = new QRecordSet("record");

        BooleanBuilder predicate = new BooleanBuilder();
//
//        if (query.getUserId() != null) {
//            predicate.and(device.userId.eq(query.getUserId()));
//        }
//
//        // name
//        Predicate pname = buildTextQuery(query.name, device.name);
//        if (pname != null) {
//            predicate.and(pname);
//        }
//
//        // description
//        Predicate pdesc = buildTextQuery(query.description, device.description);
//        if (pdesc != null) {
//            predicate.and(pdesc);
//        }
//
//        // properties
//        Predicate pprops = buildMapQuery(query.properties, device.properties);
//        if (pprops != null) {
//            predicate.and(pprops);
//        }

        //stream
        //actions
        //settings
        return predicate;
    }

    private Predicate buildTextQuery(TextQuery txt, StringPath txtfield) {

        if (txt.isEmpty()) {
            return null;
        }

        BooleanBuilder predicate = new BooleanBuilder();

        if (txt.getContains() != null) {
            predicate.and(txtfield.contains(txt.getContains()));
        }

        if (txt.getStartWith() != null) {
            predicate.and(txtfield.startsWith(txt.getStartWith()));
        }

        if (txt.getEndWith() != null) {
            predicate.and(txtfield.endsWith(txt.getEndWith()));
        }

        if (txt.getEquals() != null) {
            predicate.and(txtfield.endsWith(txt.getEquals()));
        }

        return predicate;
    }

    private Predicate buildMapQuery(MapQuery query, MapPath<String, Object, SimplePath<Object>> properties) {

        if (query.isEmpty()) {
            return null;
        }

        BooleanBuilder predicate = new BooleanBuilder();

        if (query.getContainsKey() != null) {
            predicate.and(properties.containsKey(query.getContainsKey()));
        }

        if (query.getContainsValue() != null) {
            predicate.and(properties.containsValue(query.getContainsValue()));
        }

        if (!query.getHas().isEmpty()) {
            for (Iterator<String> iterator = query.getHas().keySet().iterator(); iterator.hasNext();) {
                String key = iterator.next();
                predicate.and(properties.contains(key, query.getHas().get(key)));
            }
        }

        return predicate;
    }

}
