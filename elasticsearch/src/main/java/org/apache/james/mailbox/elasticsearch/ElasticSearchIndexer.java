/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.mailbox.elasticsearch;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;

import com.google.common.base.Preconditions;

public class ElasticSearchIndexer {

    public static final String MAILBOX_INDEX = "mailbox";
    public static final String MESSAGE_TYPE = "message";
    
    private final Node node;

    public ElasticSearchIndexer(Node node) {
        this.node = node;
    }
    
    public IndexResponse indexMessage(String id, String content) {
        checkArgument(content);
        try (Client client = node.client()) {
            return client.prepareIndex(MAILBOX_INDEX, MESSAGE_TYPE, id)
                .setSource(content)
                .get();
        }
    }
    
    public UpdateResponse updateMessage(String id, String content) {
        checkArgument(content);
        try (Client client = node.client()) {
            return client.prepareUpdate(MAILBOX_INDEX, MESSAGE_TYPE, id)
                .setDoc(content)
                .get();
        }
    }
    
    public DeleteResponse deleteMessage(String id) {
        try (Client client = node.client()) {
            return client.prepareDelete(MAILBOX_INDEX, MESSAGE_TYPE, id)
                .get();
        }
    }
    
    public DeleteByQueryResponse deleteAllWithIdStarting(String idStart) {
        try (Client client = node.client()) {
            return client.prepareDeleteByQuery(MAILBOX_INDEX)
                .setTypes(MESSAGE_TYPE)
                .setQuery(QueryBuilders.prefixQuery("_id", idStart))
                .get();
        }
    }

    private void checkArgument(String content) {
        Preconditions.checkArgument(content != null, "content should be provided");
    }
}
