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
package org.createnet.raptor.db.mapdb;

import java.io.File;
import org.createnet.raptor.db.AbstractStorage;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class MapDBStorage extends AbstractStorage {

    @Override
    public void setup(boolean forceSetup) {

        if (config.mapdb.storage.equals("file")) {
            File dir = new File(config.mapdb.storePath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new StorageException("Cannot create directory " + config.mapdb.storePath);
                }
            }
        }

        for (ConnectionId connId : ConnectionId.values()) {

            if (getConnections().containsKey(connId.name())) {
                continue;
            }

            MapDBConnection conn = new MapDBConnection(connId);
            conn.initialize(config);
            conn.setup(forceSetup);
            addConnection(conn);
        }

    }

    @Override
    public void connect() {
        connectAll();
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

}
