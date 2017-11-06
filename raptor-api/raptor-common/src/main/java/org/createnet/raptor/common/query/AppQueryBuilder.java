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
package org.createnet.raptor.common.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.StringPath;
import org.createnet.raptor.models.app.AppUser;
import org.createnet.raptor.models.app.QApp;
import org.createnet.raptor.models.app.QAppUser;
import org.createnet.raptor.models.query.AppQuery;
import org.createnet.raptor.models.query.StringListQuery;
import org.createnet.raptor.models.query.TextQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class AppQueryBuilder {

    private final AppQuery query;

    public AppQueryBuilder(AppQuery query) {
        this.query = query;
    }

    public Pageable getPaging() {

        if (query.sortBy.getFields().isEmpty()) {
            return new PageRequest(query.getOffset(), query.getLimit());
        }

        return new PageRequest(
                query.getOffset(), query.getLimit(),
                new Sort(
                        Sort.Direction.valueOf(query.sortBy.getDirection()),
                        query.sortBy.getFields()
                ));
    }

    public Predicate getPredicate() {

        QApp app = new QApp("app");

        BooleanBuilder predicate = new BooleanBuilder();

        if (query.getUserId() != null) {
            predicate.and(app.userId.eq(query.getUserId()));
        }

        // id
        Predicate pid = buildTextQuery(query.id, app.id);
        if (pid != null) {
            predicate.and(pid);
        }

        // name
        Predicate pname = buildTextQuery(query.name, app.name);
        if (pname != null) {
            predicate.and(pname);
        }

        // description
        Predicate pdesc = buildTextQuery(query.description, app.description);
        if (pdesc != null) {
            predicate.and(pdesc);
        }

        //users (id)
        Predicate users = buildAppUserListQuery(query.users, app.users);
        if (users != null) {
            predicate.and(users);
        }

        //devices (id)
        Predicate devices = buildListQuery(query.devices, app.devices);
        if (devices != null) {
            predicate.and(devices);
        }

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

        if (!txt.getIn().isEmpty()) {
            predicate.and(txtfield.in(txt.getIn()));
        }

        return predicate;
    }

    private Predicate buildListQuery(StringListQuery query, ListPath<String, StringPath> list) {

        if (query.isEmpty()) {
            return null;
        }

        BooleanBuilder predicate = new BooleanBuilder();

        if (query.getIn() != null && query.getIn().isEmpty()) {
            predicate.and(list.any().in(query.getIn()));
        }

        return predicate;
    }

    private Predicate buildAppUserListQuery(StringListQuery query, ListPath<AppUser, QAppUser> list) {

        if (query.isEmpty()) {
            return null;
        }

        BooleanBuilder predicate = new BooleanBuilder();

        if (query.getIn() != null) {
            predicate.and(list.any().uuid.in(query.getIn()));
        }

        return predicate;
    }

}
