/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sense.adapter.core;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.mdsal.dom.broker.AbstractDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SealedDOMDataBroker extends AbstractDOMDataBroker {

  public SealedDOMDataBroker(Map<LogicalDatastoreType, DOMStore> datastores) {
    super(datastores);
  }

  @Override
  protected FluentFuture<? extends CommitInfo> commit(
      DOMDataTreeWriteTransaction transaction, Collection<DOMStoreThreePhaseCommitCohort> cohorts) {
    return FluentFuture.from(
        new ListenableFuture<CommitInfo>() {
          @Override
          public void addListener(Runnable runnable, Executor executor) {}

          @Override
          public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
          }

          @Override
          public boolean isCancelled() {
            return false;
          }

          @Override
          public boolean isDone() {
            return true;
          }

          @Override
          public CommitInfo get() throws InterruptedException, ExecutionException {
            System.out.println("get result.");
            return new CommitInfo() {};
          }

          @Override
          public CommitInfo get(long timeout, TimeUnit unit)
              throws InterruptedException, ExecutionException, TimeoutException {
            return new CommitInfo() {};
          }
        });
  }

  @Override
  public DOMTransactionChain createMergingTransactionChain(DOMTransactionChainListener listener) {
    return null;
  }
}
