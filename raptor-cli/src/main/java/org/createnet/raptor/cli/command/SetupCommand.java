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
package org.createnet.raptor.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import javax.inject.Inject;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.raptor.search.raptor.search.Indexer;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Parameters(separators = "=", commandDescription = "Record changes to the repository")
public class SetupCommand implements Command {

  @Inject
  StorageService storage;
  
  @Inject
  IndexerService indexer;
  
  
  @Parameter(names = "--force", description = "Force the setup, REMOVING any previous data and configuration")
  public Boolean force = false;

  @Override
  public String getName() {
    return "setup";
  }

  @Override
  public void run() throws CommandException {

    try {
      indexer.getIndexer().setup(force);
      storage.getStorage().setup(force);
    } catch (Indexer.IndexerException | ConfigurationException | Storage.StorageException ex) {
      throw new CommandException(ex);
    }

  }
  
}

