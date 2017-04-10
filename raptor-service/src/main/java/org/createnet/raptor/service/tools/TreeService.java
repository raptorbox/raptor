/*
 * Copyright 2017 FBK/CREATE-NET.
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
package org.createnet.raptor.service.tools;

import org.createnet.raptor.service.AbstractRaptorService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.DeviceNode;
import org.createnet.raptor.indexer.Indexer;
import org.createnet.raptor.indexer.query.impl.es.TreeQuery;
import org.ehcache.Cache;
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

    @Inject
    CacheService cache;

    protected Cache<String, String> getCache() {
        return cache.getCache("tree");
    }

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

    public List<Device> getChildrenList(String id) {
        Device obj = indexer.getObject(id);
        return getChildren(obj);
    }

    public List<Device> getChildren(Device parentObject) {

        TreeQuery query = new TreeQuery();

        query.queryType = TreeQuery.TreeQueryType.Children;
        query.parentId = parentObject.parentId;
        query.id = parentObject.id;
        indexer.setQueryIndex(query, IndexerService.IndexNames.object);

        List<Indexer.IndexRecord> list = indexer.getIndexer().search(query);
        List<Device> children = new ArrayList<>();

        //load from cache
        if (getCache().containsKey(parentObject.id)) {
            List<String> childrendIds = Arrays.asList(getCache().get(parentObject.id).split(","));
            return indexer.getObjects(childrendIds);
        }

        for (Indexer.IndexRecord record : list) {
            Device child = Device.fromJSON(record.body);
            children.add(child);
        }

        return children;
    }

    public List<Device> addChildren(String parentId, List<String> childrenIds) {

        List<String> toload = new ArrayList(childrenIds);
        toload.add(parentId);

        final Device parentObject = new Device();
        List<Device> children = indexer.getObjects(toload).stream().filter((final Device o) -> {
            boolean isParent = o.id.equals(parentId);
            if (isParent) {
                parentObject.parse(o);
            }
            return !isParent;
        }).collect(Collectors.toList());

        return addChildren(parentObject, children);
    }

    public List<Device> addChildren(Device parentObject, List<Device> children) {

        List<Device> ids = getChildren(parentObject);

        children.forEach((c) -> {
            if (!ids.contains(c)) {
                ids.add(c);
            }
        });

        setChildren(parentObject, ids);

        return ids;
    }

    public List<Device> removeChildren(String parentId, List<String> childrenIds) {

        List<String> toload = new ArrayList(childrenIds);
        toload.add(parentId);

        final Device parentObject = new Device();
        List<Device> children = indexer.getObjects(toload).stream().filter((final Device o) -> {
            boolean isParent = !o.id.equals(parentId);
            if (isParent) {
                parentObject.parse(o);
            }
            return !isParent;
        }).collect(Collectors.toList());

        return removeChildren(parentObject, children);
    }

    public List<Device> removeChildren(Device parentObject, List<Device> children) {

        List<Device> ids = getChildren(parentObject);

        children.forEach((c) -> {
            if (ids.contains(c)) {
                ids.remove(c);
            }
        });

        setChildren(parentObject, ids);
        return ids;
    }

    public List<Device> setChildren(String parentId, List<String> childrenIds) {

        List<String> toload = new ArrayList(childrenIds);
        toload.add(parentId);

        final Device parentObject = new Device();
        List<Device> children = indexer.getObjects(toload).stream().filter((final Device o) -> {
            boolean isParent = !o.id.equals(parentId);
            if (isParent) {
                parentObject.parse(o);
            }
            return !isParent;
        }).collect(Collectors.toList());

        return setChildren(parentObject, children);
    }

    public List<Device> setChildren(Device parentObject, List<Device> children) {

        logger.debug("Storing {} children at {}", children.size(), parentObject.path());

        List<String> childrenIds = new ArrayList();

        generateObjectPath(parentObject);

        children.stream().forEach((c) -> {

            c.parentId = parentObject.getId();
            c.path = parentObject.isRoot() ? parentObject.id : parentObject.path();

            if (!childrenIds.contains(c.id)) {
                childrenIds.add(c.id);
            }

            logger.debug("Storing child {}.{} path: {}", c.parentId, c.id, c.path);
        });

        List<Device> toSave = new ArrayList(children);
        List<Device> previousList = getChildren(parentObject);

        previousList.stream().forEach((c) -> {
            if (!children.contains(c)) {
                c.parentId = null;
                c.path = null;
                toSave.add(c); 
           }
        });

        indexer.saveObjects(toSave, false);
        getCache().put(parentObject.id, String.join(",", childrenIds));

        logger.debug("Store children list completed for {}", parentObject.isRoot() ? parentObject.id : parentObject.path());
        return children;
    }

    public DeviceNode loadTree(Device obj) {
        return loadTree(new DeviceNode(obj));
    }

    public DeviceNode loadTree(DeviceNode node) {

        logger.debug("Loading tree for node {}", node.getCurrent().id);

        if (node.getCurrent().parentId != null && node.getParent() == null) {

            logger.debug("Loading parent {}", node.getCurrent().parentId);

            Device parent;

            parent = indexer.getObject(node.getCurrent().parentId);
            node.setParent(new DeviceNode(parent));

            if (parent.parentId != null) {
                loadTree(node.getParent());
            }

        }

        if (node.getChildren().isEmpty()) {
            List<Device> list = getChildren(node.getCurrent());
            for (Device device : list) {

                logger.debug("Loading child {}", device.id);

                DeviceNode child = new DeviceNode(device);
                child.setParent(node);

                loadTree(child);
                node.getChildren().add(child);
            }
        }

        return node;
    }

    public DeviceNode loadRoot(Device obj) {
        return loadRoot(new DeviceNode(obj));
    }

    public DeviceNode loadRoot(DeviceNode node) {

        logger.debug("Loading root path for node {}", node.getCurrent().id);

        if (node.getCurrent().parentId != null && node.getParent() == null) {

            logger.debug("Loading parent {}", node.getCurrent().parentId);

            Device parent = indexer.getObject(node.getCurrent().parentId);
            node.setParent(new DeviceNode(parent));

            if (parent.parentId != null) {
                loadRoot(node.getParent());
            }
        }

        return node;
    }

    public void generateObjectPath(Device obj) {
        if (obj.parentId != null) {
            DeviceNode rootNode = loadRoot(obj);
            String path = rootNode.path();
            obj.path = path;
            logger.debug("Generated new path {}", path);
        }
    }

    /**
     * @TODO Remove on upgrade to ES 5.x
     */
//    @Deprecated
//    protected boolean lookupGroupItem(String parentId, int childLength) {
//        int max = 25, curr = max, wait = 150; //ms
//        while (curr > 0) {
//            try {
//
//                List<Device> objs = getChildren(new Device(parentId));
//
//                if (objs.size() == childLength) {
//                    logger.warn("Object {} avail in index after {}ms", parentId, (max - curr) * wait);
//                    return true;
//                }
//
//                logger.debug("Expecting {} chidlren, found {}. Waiting for index update", childLength, objs.size());
//                Thread.sleep(wait);
//
//            } catch (Exception ex) {
//            } finally {
//                curr--;
//            }
//        }
//        return false;
//    }
}
