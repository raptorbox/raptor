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
package org.createnet.raptor.search;

import java.util.List;
import org.createnet.raptor.search.impl.ElasticSearchIndexer;
import org.createnet.raptor.search.impl.IndexerConfiguration;
import org.createnet.raptor.search.query.Query;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class IndexerProvider extends AbstractIndexer {

    private Indexer indexer;

    @Override
    public void initialize(IndexerConfiguration configuration) throws IndexerException {
        super.initialize(configuration);

        switch (configuration.type) {
            case "elasticsearch":
                indexer = new ElasticSearchIndexer();
                break;
            default:
                throw new IndexerException("Indexer type " + configuration.type + " is not supported.");
        }

        indexer.initialize(configuration);

    }

    @Override
    public void open() throws IndexerException {
        indexer.open();
    }

    @Override
    public void setup(boolean forceSetup) throws IndexerException {
        indexer.setup(forceSetup);
    }

    @Override
    public void close() throws IndexerException {
        indexer.close();
    }

    @Override
    public void save(IndexRecord record) throws IndexerException {

        try {
            record.validate();
            indexer.save(record);
        } catch (IndexRecordValidationException ex) {
            throw new IndexerException(ex);
        }

    }

    @Override
    public void delete(IndexRecord record) throws IndexerException {
        indexer.delete(record);
    }

    @Override
    public void batch(List<IndexOperation> list) throws IndexerException {
        indexer.batch(list);
    }

    @Override
    public List<IndexRecord> search(Query query) throws SearchException {
        return indexer.search(query);
    }

}
