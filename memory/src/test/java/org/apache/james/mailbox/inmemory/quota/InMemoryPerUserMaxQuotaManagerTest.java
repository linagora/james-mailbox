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

package org.apache.james.mailbox.inmemory.quota;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.james.mailbox.model.Quota;
import org.apache.james.mailbox.model.QuotaRoot;
import org.apache.james.mailbox.store.quota.QuotaRootImpl;
import org.junit.Before;
import org.junit.Test;

public class InMemoryPerUserMaxQuotaManagerTest {

    private InMemoryPerUserMaxQuotaManager perUserMaxQuotaManager;
    private QuotaRoot quotaRoot;

    @Before
    public void setUp() {
        perUserMaxQuotaManager = new InMemoryPerUserMaxQuotaManager();
        quotaRoot = QuotaRootImpl.quotaRoot("benwa");
    }

    @Test
    public void getMaxMessageShouldReturnUnlimitedWhenNoDefaultValue() throws Exception {
        assertThat(perUserMaxQuotaManager.getMaxMessage(quotaRoot)).isEqualTo(Quota.UNLIMITED);
    }

    @Test
    public void getMaxStorageShouldReturnUnlimitedWhenNoDefaultValue() throws Exception {
        assertThat(perUserMaxQuotaManager.getMaxStorage(quotaRoot)).isEqualTo(Quota.UNLIMITED);
    }

    @Test
    public void getMaxMessageShouldReturnDefaultWhenNoValue() throws Exception {
        perUserMaxQuotaManager.setDefaultMaxMessage(36);
        assertThat(perUserMaxQuotaManager.getMaxMessage(quotaRoot)).isEqualTo(36);
    }

    @Test
    public void getMaxStorageShouldReturnDefaultWhenNoValue() throws Exception {
        perUserMaxQuotaManager.setDefaultMaxStorage(36);
        assertThat(perUserMaxQuotaManager.getMaxStorage(quotaRoot)).isEqualTo(36);
    }

    @Test
    public void getMaxMessageShouldReturnProvidedValue() throws Exception {
        perUserMaxQuotaManager.setMaxMessage(quotaRoot, 36);
        assertThat(perUserMaxQuotaManager.getMaxMessage(quotaRoot)).isEqualTo(36);
    }

    @Test
    public void getMaxStorageShouldReturnProvidedValue() throws Exception {
        perUserMaxQuotaManager.setMaxStorage(quotaRoot, 36);
        assertThat(perUserMaxQuotaManager.getMaxStorage(quotaRoot)).isEqualTo(36);
    }

}
