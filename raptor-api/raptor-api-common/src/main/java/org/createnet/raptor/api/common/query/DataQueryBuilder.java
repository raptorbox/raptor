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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.createnet.raptor.models.query.BoolQuery;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.models.query.GeoQuery;
import org.createnet.raptor.models.query.IQuery;
import org.createnet.raptor.models.query.MapQuery;
import org.createnet.raptor.models.query.NumberQuery;
import org.createnet.raptor.models.query.TextQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DataQueryBuilder extends BaseQueryBuilder {

    @JsonIgnore
    final private Logger log = LoggerFactory.getLogger(DataQueryBuilder.class);

    private final DataQuery query;
    private final List<CriteriaDefinition> criteria = new ArrayList();

    public DataQueryBuilder(DataQuery query) {
        this.query = query;
    }

    protected void addCriteria(CriteriaDefinition c) {
        criteria.add(c);
    }

    protected Criteria[] getCriteria() {
        return criteria.toArray(new Criteria[criteria.size()]);
    }

    public Pageable getPaging() {
        return getPaging(query);
    }

    public Query getQuery() {

        Query q = new Query();
        q.with(this.getPaging());

        if (query.getUserId() != null) {
            addCriteria(Criteria.where("userId").is(query.getUserId()));
        }

        if (query.getStreamId() != null) {
            addCriteria(Criteria.where("streamId").is(query.getStreamId()));
        }

        if (query.getDeviceId() != null) {
            addCriteria(Criteria.where("deviceId").is(query.getDeviceId()));
        }

        if (!query.getTimestamp().isEmpty()) {
            // between
            if (query.getTimestamp().getBetween()[0] != null && query.getTimestamp().getBetween()[1] != null) {
                Date t0 = Date.from(Instant.ofEpochMilli(query.getTimestamp().getBetween()[0].longValue()));
                Date t1 = Date.from(Instant.ofEpochMilli(query.getTimestamp().getBetween()[1].longValue()));
                addCriteria(Criteria.where("timestamp").lt(t1).gt(t0));
            }
        }

        if (!query.getLocation().isEmpty()) {
            GeoQuery.Distance d = query.getLocation().getDistance();
            if (d.center != null) {
                addCriteria(Criteria.where("location").withinSphere(
                        new Circle(d.center, 
                                new Distance(d.radius, d.unit)
                        )
                ));
            }
        }

        for (Map.Entry<String, IQuery> en : query.getChannels().entrySet()) {

            String channelName = en.getKey();
            String channelFieldName = "channels." + channelName;

            IQuery channelQuery = en.getValue();

            if (channelQuery instanceof TextQuery) {

                TextQuery txtQuery = (TextQuery) channelQuery;

                if (txtQuery.getContains() != null) {
//                    addCriteria(TextCriteria.forDefaultLanguage().matching(txtQuery.getContains()));
                    addCriteria(Criteria.where(channelFieldName).is(txtQuery.getContains()));
                }
                if (txtQuery.getStartWith() != null) {
                    addCriteria(Criteria.where(channelFieldName).regex(String.format("/^%s/", txtQuery.getStartWith())));
                }
                if (txtQuery.getEndWith() != null) {
                    addCriteria(Criteria.where(channelFieldName).regex(String.format("/%s$/", txtQuery.getEndWith())));
                }
            }

            if (channelQuery instanceof BoolQuery) {
                BoolQuery boolQuery = (BoolQuery) channelQuery;
                addCriteria(Criteria.where(channelFieldName).is(boolQuery.getMatch()));
            }

            if (channelQuery instanceof NumberQuery) {
                NumberQuery numQuery = (NumberQuery) channelQuery;
                if (numQuery.getBetween() != null) {
                    addCriteria(Criteria.where(channelFieldName).gte(numQuery.getBetween()[0]).lte(numQuery.getBetween()[1]));
                }
            }

        }

        if (criteria.isEmpty()) {
            return null;
        }

        q.addCriteria(new Criteria().andOperator(getCriteria()));

        log.debug("Mongodb data query: {}", q);

        return q;
    }

    public Predicate getPredicate() {
        throw new RuntimeException("querydsl geoquery is missing?");
//        QRecordSet record = new QRecordSet("record");
//
//        BooleanBuilder predicate = new BooleanBuilder();
//
//        if (query.getUserId() != null) {
//            predicate.and(record.userId.eq(query.getUserId()));
//        }
//        
//        if (!query.getTimestamp().isEmpty()) {
//            
//            // between
//            if(query.getTimestamp().getBetween().length > 0) {
//                Date t0 = Date.from(Instant.ofEpochMilli(query.getTimestamp().getBetween()[0].longValue()));
//                Date t1 = Date.from(Instant.ofEpochMilli(query.getTimestamp().getBetween()[1].longValue()));
//                predicate.and(record.timestamp.between(t0, t1));
//            }
//            
//        }
//        
//        if (!query.getLocation().isEmpty()) {
//            GeoQuery.Distance d = query.getLocation().getDistance();
//            if (d.center != null) {
//                predicate.and(MongodbExpressions.near(record.location, 0, 0));
//                NearQuery nearQuery = NearQuery
//                        .near(d.center)
//                        .maxDistance(new Distance(d.radius, Metrics.KILOMETERS));
//            }
//        }
//        
//        
//        return predicate;
    }

    private Query buildTextCriteria(String fieldName, TextQuery txt) {

        if (txt.isEmpty()) {
            return null;
        }

        Query q = new Query();

        if (txt.getContains() != null) {
            q.addCriteria(TextCriteria.forDefaultLanguage().matching(fieldName));
        }

        if (txt.getStartWith() != null) {
        }

        if (txt.getEndWith() != null) {
        }

        if (txt.getEquals() != null) {
        }

        return q;
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
