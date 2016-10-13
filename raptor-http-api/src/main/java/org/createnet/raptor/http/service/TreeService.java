/*
 * Copyright 2016 CREATE-NET.
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
package org.createnet.raptor.http.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.ServiceObjectNode;
import org.createnet.raptor.search.raptor.search.Indexer;
import org.createnet.raptor.search.raptor.search.query.impl.es.TreeQuery;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author l
 */
@Service
public class TreeService implements RaptorService {

    private final static Logger logger = LoggerFactory.getLogger(TreeService.class);

    @PostConstruct
    @Override
    public void initialize() {}

    @PreDestroy
    @Override
    public void shutdown() {}    
    
    @Inject
    IndexerService indexer;

    public class TreeException extends RuntimeException {

        public TreeException(String message) {
            super(message);
        }

        public TreeException(String message, Throwable cause) {
            super(message, cause);
        }

        public TreeException(Throwable cause) {
            super(cause);
        }

    }

    public List<String> getChildrenList(String id) {
        ServiceObject obj = indexer.getObject(id);
        return getChildrenList(obj);
    }

    public List<String> getChildrenList(ServiceObject parentObject) {
        TreeQuery query = new TreeQuery();
        query.queryType = TreeQuery.TreeQueryType.Children;
        query.parentId = parentObject.parentId;
        query.id = parentObject.id;
        indexer.setQueryIndex(query, IndexerService.IndexNames.object);

        List<String> list = indexer.getIndexer().search(query);

        List<String> children = new ArrayList<>();
        for (String raw : list) {
            try {
                TreeQuery.TreeRecordBody record = ServiceObject.getMapper().readValue(raw, TreeQuery.TreeRecordBody.class);
                children.add(record.id);
            } catch (IOException ex) {
                logger.error("Cannot parse group record");
                throw new RaptorComponent.ParserException(ex);
            }
        }

        return children;

    }

    public List<ServiceObject> getChildren(ServiceObject parentObject) {
        List<String> ids = getChildrenList(parentObject);

        List<ServiceObject> list = new ArrayList();

        if (!ids.isEmpty()) {
            list = indexer.getObjects(ids);
        }

        return list;

    }

    public void setChildrenList(ServiceObject obj, List<String> list) {

        logger.debug("Storing {} children for {}", list.size(), obj != null ? obj.id : "<root>");

        List<String> previousList = getChildrenList(obj);

        List<Indexer.IndexOperation> ops = new ArrayList();

        // drop ALL the oldies
        List<String> toRemoveList = new ArrayList(previousList);
        if (!toRemoveList.isEmpty()) {
            for (String removeId : toRemoveList) {
                Indexer.IndexRecord record = indexer.getIndexRecord(IndexerService.IndexNames.object);
                record.id = removeId;
                Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.DELETE, record);
                ops.add(op);
            }
        }

        // insert the new ones
        for (String childId : list) {

            Indexer.IndexRecord record = indexer.getIndexRecord(IndexerService.IndexNames.object);
            record.id = childId;
            record.isNew(true);

            TreeQuery.TreeRecordBody treeRecord = new TreeQuery.TreeRecordBody();
            treeRecord.id = childId;
            String parentId = obj != null ? obj.id : null;
            treeRecord.parentId = parentId;
            treeRecord.path = (parentId == null ? "" : parentId + "/") + childId;
            try {
                record.body = ServiceObject.getMapper().writeValueAsString(treeRecord);
            } catch (JsonProcessingException ex) {
                throw new TreeException(ex);
            }
            Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.CREATE, record);
            ops.add(op);
        }

        try {
            indexer.getIndexer().batch(ops);
        } catch (Indexer.IndexerException ex) {
            throw new TreeException(ex);
        }

        lookupGroupItem(obj.id, list.size());
        logger.debug("Store children list completed");
    }

    public ServiceObjectNode loadTree(ServiceObject obj) {
        return loadTree(new ServiceObjectNode(obj));
    }

    protected ServiceObject getObject(String id) {
        return indexer.getObject(id);

    }

    protected List<ServiceObject> getObjects(List<String> ids) {
        return indexer.getObjects(ids);
    }

    public ServiceObjectNode loadTree(ServiceObjectNode node) {

        logger.debug("Loading tree for node {}", node.getCurrent().id);

        if (node.getCurrent().parentId != null && node.getParent() == null) {

            logger.debug("Loading parent {}", node.getCurrent().parentId);

            ServiceObject parent;

            parent = getObject(node.getCurrent().parentId);
            node.setParent(new ServiceObjectNode(parent));

            if (parent.parentId != null) {
                loadTree(node.getParent());
            }

        }

        if (node.getChildren().isEmpty()) {
            List<ServiceObject> list = getChildren(node.getCurrent());
            for (ServiceObject serviceObject : list) {

                logger.debug("Loading child {}", serviceObject.id);

                ServiceObjectNode child = new ServiceObjectNode(serviceObject);
                child.setParent(node);

                loadTree(child);
                node.getChildren().add(child);
            }
        }

        return node;
    }

    public ServiceObjectNode loadRoot(ServiceObjectNode node) {

        logger.debug("Loading root path for node {}", node.getCurrent().id);

        if (node.getCurrent().parentId != null && node.getParent() == null) {

            logger.debug("Loading parent {}", node.getCurrent().parentId);

            ServiceObject parent = getObject(node.getCurrent().parentId);
            node.setParent(new ServiceObjectNode(parent));

            if (parent.parentId != null) {
                loadRoot(node.getParent());
            }
        }

        return node;
    }

    /**
     * @TODO Remove on upgrade to ES 5.x
     */
    @Deprecated
    protected boolean lookupGroupItem(String parentId, int childLength) {
        int max = 10, curr = max, wait = 500; //ms
        while (curr > 0) {
            try {

                List<String> objs = getChildrenList(new ServiceObject(parentId));
                if (objs.size() == childLength) {
                    logger.warn("Object {} avail in index after {}ms", parentId, (max - curr) * wait);
                    return true;
                }

                logger.debug("Expecting {} chidlren, found {}. Waiting for index update", childLength, objs.size());
                Thread.sleep(wait);

            } catch (Exception ex) {
            } finally {
                curr--;
            }
        }
        return false;
    }

}
