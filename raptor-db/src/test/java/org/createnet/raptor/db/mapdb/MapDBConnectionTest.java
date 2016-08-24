/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
package org.createnet.raptor.db.mapdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.db.query.BaseQuery;
import org.createnet.raptor.db.query.ListQuery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class MapDBConnectionTest {

  static final protected ObjectMapper mapper = Storage.mapper;

  final StorageConfiguration config = new StorageConfiguration();
  MapDBConnection instance;

  public MapDBConnectionTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws Storage.StorageException {
    
    loadConfig();
    
    new File(config.mapdb.storePath).mkdirs();

    instance = new MapDBConnection(Storage.ConnectionId.objects);
    instance.initialize(config);
    instance.setup(true);
    instance.connect();
  }

  @After
  public void tearDown() {
    instance.disconnect();
    instance.destroy();
  }

  @Test
  public void testListObjects() throws Exception {

    populateObjectData();

    ListQuery query = BaseQuery.createQuery(new ListQuery.QueryParam[]{
      new ListQuery.QueryParam("userId", "test-user"),
    });

    List<JsonNode> result = instance.list(query);
    assertEquals(result.size(), 3);
    
  }

  private void populateObjectData() throws IOException, Storage.StorageException {
    ObjectNode obj = loadData("model");
    
    obj.put("id", "test1");
    obj.put("id", "test-user");
    instance.set(obj.get("id").asText(), obj, 0);    
    
    obj.put("id", "test2");
    obj.put("id", "test-user-x");
    instance.set(obj.get("id").asText(), obj, 0);    
    
    obj.put("id", "test3");
    obj.put("id", "test-user");
    instance.set(obj.get("id").asText(), obj, 0);    
  }

  protected ObjectNode loadData(String filename) throws IOException {

    String filepath = filename + ".json";
    URL res = getClass().getClassLoader().getResource(filepath);

    if (res == null) {
      throw new IOException("Cannot load " + filepath);
    }

    String strpath = res.getPath();

    Path path = Paths.get(strpath);
    byte[] content = Files.readAllBytes(path);

    return (ObjectNode) mapper.readTree(content);
  }

  private void loadConfig() {

    config.mapdb.storage = "file";
    config.mapdb.storePath = "/tmp/raptor-test";
    
    List<List<String>> indices;
    List<String> idx;
    
    
    // objects index
    indices = new ArrayList();
    
    idx = new ArrayList();
    idx.add("userId");
    indices.add(idx);
    
    idx = new ArrayList();
    idx.add("userId");
    indices.add(idx);
    
    config.mapdb.indices.put(Storage.ConnectionId.objects.name(), indices);
    
    // data stream index
    indices = new ArrayList();
    
    idx = new ArrayList();
    idx.add("userId");
    idx.add("objectId");
    indices.add(idx);
    
    idx = new ArrayList();
    idx.add("userId");
    idx.add("objectId");
    idx.add("streamId");
    indices.add(idx);
    
    config.mapdb.indices.put(Storage.ConnectionId.data.name(), indices);
    
  }

}
