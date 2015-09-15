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

import com.google.common.base.Preconditions;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.QuotaRoot;
import org.apache.james.mailbox.store.quota.CurrentQuotaCalculator;
import org.apache.james.mailbox.store.quota.StoreCurrentQuotaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCurrentQuotaManager implements StoreCurrentQuotaManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCurrentQuotaManager.class);

    private final ConcurrentHashMap<QuotaRoot, Entry> quotaMap;
    private CurrentQuotaCalculator quotaCalculator;
    private MailboxManager mailboxManager;
    private boolean calculateOnUnknownQuota = true;

    public InMemoryCurrentQuotaManager() {
        this.quotaMap = new ConcurrentHashMap<QuotaRoot, Entry>();
    }

    @Inject
    public void setQuotaCalculator(CurrentQuotaCalculator quotaCalculator) {
        this.quotaCalculator = quotaCalculator;
    }

    @Inject
    public void setMailboxManager(MailboxManager mailboxManager) {
        this.mailboxManager = mailboxManager;
    }

    public void setCalculateOnUnknownQuota(boolean calculateOnUnknownQuota) {
        this.calculateOnUnknownQuota = calculateOnUnknownQuota;
    }

    @Override
    public void increase(QuotaRoot quotaRoot, long count, long size) throws MailboxException {
        checkArguments(count, size);
        doIncrease(quotaRoot, count, size);
    }

    @Override
    public void decrease(QuotaRoot quotaRoot, long count, long size) throws MailboxException {
        checkArguments(count, size);
        doIncrease(quotaRoot, -count, -size);
    }

    @Override
    public Long getCurrentMessageCount(QuotaRoot quotaRoot) throws MailboxException {
        Entry entry = quotaMap.get(quotaRoot);
        if (entry == null) {
            if (calculateOnUnknownQuota) {
                entry = concurrentAwareEntryInitialization(quotaRoot);
            } else {
                return 0L;
            }
        }
        return entry.getCount().get();
    }

    @Override
    public Long getCurrentStorage(QuotaRoot quotaRoot) throws MailboxException {
        Entry entry = quotaMap.get(quotaRoot);
        if (entry == null) {
            if (calculateOnUnknownQuota) {
                entry = concurrentAwareEntryInitialization(quotaRoot);
            } else {
                return 0L;
            }
        }
        return entry.getSize().get();
    }

    private void doIncrease(QuotaRoot quotaRoot, long count, long size) throws MailboxException {
        Entry entry = quotaMap.get(quotaRoot);
        if (entry == null) {
            entry = concurrentAwareEntryInitialization(quotaRoot);
        }
        entry.getCount().addAndGet(count);
        entry.getSize().addAndGet(size);
    }

    private Entry concurrentAwareEntryInitialization(QuotaRoot quotaRoot) throws MailboxException {
        Entry entry = computeDefaultEntry(quotaRoot);
        Entry previousEntry =  quotaMap.putIfAbsent(quotaRoot, entry);
        if (previousEntry != null) {
            return previousEntry;
        }
        return entry;
    }

    private Entry computeDefaultEntry(QuotaRoot quotaRoot) throws MailboxException {
        MailboxSession mailboxSession = mailboxManager.createSystemSession(quotaRoot.getValue(), LOGGER);
        if (calculateOnUnknownQuota) {
            return new Entry(quotaCalculator.recalculateCurrentQuotas(quotaRoot, mailboxSession));
        } else {
            return new Entry();
        }
    }

    private void checkArguments(long count, long size) {
        Preconditions.checkArgument(count > 0, "Count should be positive");
        Preconditions.checkArgument(size > 0, "Size should be positive");
    }

    class Entry {
        private final AtomicLong count;
        private final AtomicLong size;

        public Entry() {
            this.count = new AtomicLong(0L);
            this.size = new AtomicLong(0L);
        }

        public Entry(CurrentQuotaCalculator.CurrentQuotas currentQuotas) {
            this.count = new AtomicLong(currentQuotas.getCount());
            this.size = new AtomicLong(currentQuotas.getSize());
        }

        public AtomicLong getCount() {
            return count;
        }

        public AtomicLong getSize() {
            return size;
        }
    }
}
