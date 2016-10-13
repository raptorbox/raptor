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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.ServiceObjectNode;
import org.createnet.raptor.search.query.impl.es.TreeQuery;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author l
 */
@Service
public class TreeService extends AbstractRaptorService {

    private final static Logger logger = LoggerFactory.getLogger(TreeService.class);

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

    public List<ServiceObject> getChildrenList(String id) {
        ServiceObject obj = indexer.getObject(id);
        return getChildren(obj);
    }

    public List<ServiceObject> getChildren(ServiceObject parentObject) {

        TreeQuery query = new TreeQuery();

        query.queryType = TreeQuery.TreeQueryType.Children;
        query.parentId = parentObject.parentId;
        query.id = parentObject.id;
        indexer.setQueryIndex(query, IndexerService.IndexNames.object);

        List<String> list = indexer.getIndexer().search(query);
        List<ServiceObject> children = new ArrayList<>();
        for (String raw : list) {
            ServiceObject child = ServiceObject.fromJSON(raw);
            children.add(child);
        }

        return children;
    }

    public List<ServiceObject> addChildren(String parentId, List<String> childrenIds) {

        List<String> toload = new ArrayList(childrenIds);
        toload.add(parentId);

        final ServiceObject parentObject = new ServiceObject();
        List<ServiceObject> children = indexer.getObjects(toload).stream().filter((final ServiceObject o) -> {
            boolean isParent = !o.id.equals(parentId);
            if (isParent) {
                parentObject.parse(o);
            }
            return !isParent;
        }).collect(Collectors.toList());

        return addChildren(parentObject, children);
    }

    public List<ServiceObject> addChildren(ServiceObject parentObject, List<ServiceObject> children) {

        List<ServiceObject> ids = getChildren(parentObject);

        children.forEach((c) -> {
            if (!ids.contains(c)) {
                ids.add(c);
            }
        });

        setChildren(parentObject, ids);

        return ids;
    }

    public List<ServiceObject> removeChildren(String parentId, List<String> childrenIds) {

        List<String> toload = new ArrayList(childrenIds);
        toload.add(parentId);

        final ServiceObject parentObject = new ServiceObject();
        List<ServiceObject> children = indexer.getObjects(toload).stream().filter((final ServiceObject o) -> {
            boolean isParent = !o.id.equals(parentId);
            if (isParent) {
                parentObject.parse(o);
            }
            return !isParent;
        }).collect(Collectors.toList());

        return removeChildren(parentObject, children);
    }

    public List<ServiceObject> removeChildren(ServiceObject parentObject, List<ServiceObject> children) {

        List<ServiceObject> ids = getChildren(parentObject);

        children.forEach((c) -> {
            if (ids.contains(c)) {
                ids.remove(c);
            }
        });

        setChildren(parentObject, ids);
        return ids;
    }

    
    public List<ServiceObject> setChildren(String parentId, List<String> childrenIds) {

        List<String> toload = new ArrayList(childrenIds);
        toload.add(parentId);

        final ServiceObject parentObject = new ServiceObject();
        List<ServiceObject> children = indexer.getObjects(toload).stream().filter((final ServiceObject o) -> {
            boolean isParent = !o.id.equals(parentId);
            if (isParent) {
                parentObject.parse(o);
            }
            return !isParent;
        }).collect(Collectors.toList());
        
        return setChildren(parentObject, children);
    }
    
    public List<ServiceObject> setChildren(ServiceObject obj, List<ServiceObject> list) {

        generateObjectPath(obj);

        logger.debug("Storing {} children at {}", list.size(), obj.path());
        
        list.stream().forEach((c) -> {
            c.parentId = obj.getId();
            c.path = (obj.isRoot() ? "" : "/") + obj.getId();
        });

        List<ServiceObject> toSave = new ArrayList(list);
        List<ServiceObject> previousList = getChildren(obj);

        previousList.stream().forEach((c) -> {
            if (!list.contains(c)) {
                c.parentId = null;
                c.path = null;
                toSave.add(c);
            }
        });

        indexer.saveObjects(toSave, false);
        lookupGroupItem(obj.id, list.size());

        logger.debug("Store children list completed");
        return list;
    }

    public ServiceObjectNode loadTree(ServiceObject obj) {
        return loadTree(new ServiceObjectNode(obj));
    }

    public ServiceObjectNode loadTree(ServiceObjectNode node) {

        logger.debug("Loading tree for node {}", node.getCurrent().id);

        if (node.getCurrent().parentId != null && node.getParent() == null) {

            logger.debug("Loading parent {}", node.getCurrent().parentId);

            ServiceObject parent;

            parent = indexer.getObject(node.getCurrent().parentId);
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

    public ServiceObjectNode loadRoot(ServiceObject obj) {
        return loadRoot(new ServiceObjectNode(obj));
    }

    public ServiceObjectNode loadRoot(ServiceObjectNode node) {

        logger.debug("Loading root path for node {}", node.getCurrent().id);

        if (node.getCurrent().parentId != null && node.getParent() == null) {

            logger.debug("Loading parent {}", node.getCurrent().parentId);

            ServiceObject parent = indexer.getObject(node.getCurrent().parentId);
            node.setParent(new ServiceObjectNode(parent));

            if (parent.parentId != null) {
                loadRoot(node.getParent());
            }
        }

        return node;
    }

    public void generateObjectPath(ServiceObject obj) {
        if(obj.parentId != null) {
            ServiceObjectNode rootNode = loadRoot(obj);
            String path = rootNode.path();
            obj.path = path;
            logger.debug("Generated new path {}", path);
        }
    }

    /**
     * @TODO Remove on upgrade to ES 5.x
     */
    @Deprecated
    protected boolean lookupGroupItem(String parentId, int childLength) {
        int max = 10, curr = max, wait = 500; //ms
        while (curr > 0) {
            try {

                List<ServiceObject> objs = getChildren(new ServiceObject(parentId));

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
