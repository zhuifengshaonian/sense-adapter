/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sense.adapter.core;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SealedDOMTransactionChain implements DOMTransactionChain {

  private NormalizedNode<?, ?> temp;

  public static DOMTransactionChain getInstance() {
    return new com.tencent.sense.adapter.core.SealedDOMTransactionChain();
  }

  @Override
  public DOMDataTreeReadTransaction newReadOnlyTransaction() {
    return new DOMDataTreeReadTransaction() {
      @Override
      public void close() {}

      @Override
      public FluentFuture<Optional<NormalizedNode<?, ?>>> read(
          LogicalDatastoreType store, YangInstanceIdentifier path) {
        return FluentFuture.from(
            new ListenableFuture<Optional<NormalizedNode<?, ?>>>() {
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
              public Optional<NormalizedNode<?, ?>> get()
                  throws InterruptedException, ExecutionException {
                return Optional.of(temp);
              }

              @Override
              public Optional<NormalizedNode<?, ?>> get(long timeout, TimeUnit unit)
                  throws InterruptedException, ExecutionException, TimeoutException {
                return Optional.of(temp);
              }
            });
      }

      @Override
      public FluentFuture<Boolean> exists(LogicalDatastoreType store, YangInstanceIdentifier path) {
        return FluentFuture.from(
            new ListenableFuture() {
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
              public Object get() throws InterruptedException, ExecutionException {
                return true;
              }

              @Override
              public Object get(long timeout, TimeUnit unit)
                  throws InterruptedException, ExecutionException, TimeoutException {
                return true;
              }

              @Override
              public void addListener(Runnable runnable, Executor executor) {}
            });
      }

      @Override
      public Object getIdentifier() {
        return temp.getIdentifier();
      }
    };
  }

  @Override
  public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
    return new DOMDataTreeWriteTransaction() {

      @Override
      public Object getIdentifier() {
        return temp.getIdentifier();
      }

      @Override
      public void put(
          LogicalDatastoreType store, YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
        temp = data;
      }

      @Override
      public void merge(
          LogicalDatastoreType store, YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
        temp = data;
      }

      @Override
      public void delete(LogicalDatastoreType store, YangInstanceIdentifier path) {
        temp = null;
      }

      @Override
      public @NonNull FluentFuture<? extends CommitInfo> commit() {
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
                return new CommitInfo() {
                  @Override
                  public int hashCode() {
                    return super.hashCode();
                  }
                };
              }

              @Override
              public CommitInfo get(long timeout, TimeUnit unit)
                  throws InterruptedException, ExecutionException, TimeoutException {
                return new CommitInfo() {
                  @Override
                  public int hashCode() {
                    return super.hashCode();
                  }
                };
              }
            });
      }

      @Override
      public boolean cancel() {
        return false;
      }
    };
  }

  @Override
  public DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
    return new DOMDataTreeReadWriteTransaction() {

      @Override
      public Object getIdentifier() {
        return null;
      }

      @Override
      public void put(
          LogicalDatastoreType store, YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
        temp = data;
      }

      @Override
      public void merge(
          LogicalDatastoreType store, YangInstanceIdentifier path, NormalizedNode<?, ?> data) {
        temp = data;
      }

      @Override
      public void delete(LogicalDatastoreType store, YangInstanceIdentifier path) {
        temp = null;
      }

      @Override
      public @NonNull FluentFuture<? extends CommitInfo> commit() {
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
                return new CommitInfo() {
                  @Override
                  public int hashCode() {
                    return super.hashCode();
                  }
                };
              }

              @Override
              public CommitInfo get(long timeout, TimeUnit unit)
                  throws InterruptedException, ExecutionException, TimeoutException {
                return new CommitInfo() {
                  @Override
                  public int hashCode() {
                    return super.hashCode();
                  }
                };
              }
            });
      }

      @Override
      public boolean cancel() {
        return false;
      }

      @Override
      public FluentFuture<Optional<NormalizedNode<?, ?>>> read(
          LogicalDatastoreType store, YangInstanceIdentifier path) {
        return newReadOnlyTransaction().read(store, path);
      }

      @Override
      public FluentFuture<Boolean> exists(LogicalDatastoreType store, YangInstanceIdentifier path) {
        return newReadOnlyTransaction().exists(store, path);
      }
    };
  }

  @Override
  public void close() {}
}
