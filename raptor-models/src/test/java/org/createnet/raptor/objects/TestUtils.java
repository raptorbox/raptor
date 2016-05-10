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
package org.createnet.raptor.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class TestUtils {
  
  protected String modelFilename = "model.json";
  protected String dataFilename = "record.json";
  
  protected JsonNode jsonServiceObject = null;
  protected ServiceObject serviceObject;
  protected JsonNode jsonData;
  
  ObjectMapper mapper = new ObjectMapper();  
  
  protected void loadObject() throws IOException {
    
    Path path = Paths.get(getClass().getClassLoader().getResource(modelFilename).getPath());
    byte[] content = Files.readAllBytes(path);
    
    jsonServiceObject = mapper.readTree(content);
    serviceObject = new ServiceObject();
    
  };
  
  protected void loadData() throws IOException {
    
    Path path = Paths.get(getClass().getClassLoader().getResource(dataFilename).getPath());
    byte[] content = Files.readAllBytes(path);
    
    jsonData = mapper.readTree(content);  
  };
  
}
