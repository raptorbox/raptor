/*
 * Copyright 2016 CREATE-NET
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.createnet.raptor.models.objects.ServiceObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class GroupService implements RaptorService{

  private final Logger logger = LoggerFactory.getLogger(GroupService.class);

  @Inject
  ConfigurationService configuration;

  @Inject
  IndexerService indexer;
    
  @PostConstruct
  @Override
  public void initialize() throws ServiceException {}

  @PreDestroy
  @Override
  public void shutdown() throws ServiceException {}
  
  public void save(ServiceObject obj) {
    
  }
  
  public void load(ServiceObject object) {

  }
  
}
