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
package org.createnet.raptor.http.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Map;
import javax.inject.Singleton;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */

@Service
@Singleton
public class ConfigurationService {
  
  final private String basePath = "/etc/raptor/";
  final private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  
  protected String getPath(String filename) {
    return basePath + filename + ".yml";
  }
  
  public StorageConfiguration getStorage() throws IOException {
    return mapper.readValue(getPath("storage"), StorageConfiguration.class);
  }
  
}
