package com.tencent.sense.adapter.core;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.*;

public class SealedTransactionChain implements TransactionChain {

    private static final Map<InstanceIdentifier, DataObject> operData = new HashMap<InstanceIdentifier, DataObject>();
    private static final Map<InstanceIdentifier, DataObject> cfgData = new HashMap<InstanceIdentifier, DataObject>();

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        return new SealedReadTransaction(cfgData, operData);
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return new SealedWriteTransaction(cfgData, operData);
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return new SealedReadWriteTransaction(cfgData, operData);
    }

    @Override
    public void close() {

    }
}

class SealedReadTransaction implements ReadTransaction{

    private volatile Map<InstanceIdentifier, DataObject> operData;
    private volatile Map<InstanceIdentifier, DataObject> cfgData;
    private volatile UUID id;

    SealedReadTransaction(@NonNull Map<InstanceIdentifier, DataObject> cfgData, @NonNull Map<InstanceIdentifier, DataObject> operData){
        this.cfgData = cfgData;
        this.operData = operData;
        this.id = UUID.randomUUID();
    }

    @Override
    public void close() {
        this.operData = null;
        this.cfgData = null;
        this.id = null;
    }

    @Override
    public @NonNull <T extends DataObject> FluentFuture<Optional<T>> read(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path) {
        switch (store){
            case OPERATIONAL:
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
                                return operData.get(path);
                            }

                            @Override
                            public Object get(long timeout, TimeUnit unit)
                                    throws InterruptedException, ExecutionException, TimeoutException {
                                return operData.get(path);
                            }

                            @Override
                            public void addListener(Runnable runnable, Executor executor) {}
                        });
            case CONFIGURATION:
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
                                return cfgData.get(path);
                            }

                            @Override
                            public Object get(long timeout, TimeUnit unit)
                                    throws InterruptedException, ExecutionException, TimeoutException {
                                return cfgData.get(path);
                            }

                            @Override
                            public void addListener(Runnable runnable, Executor executor) {}
                        });

        }
        throw new RuntimeException("invalid data store type");
    }

    @Override
    public @NonNull FluentFuture<Boolean> exists(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<?> path) {
        return FluentFuture.from(new ListenableFuture() {
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

    @NonNull
    @Override
    public Object getIdentifier() {
        return id;
    }
}

class SealedWriteTransaction implements WriteTransaction{

    private final SealedReadWriteTransaction rwtransaction;

    SealedWriteTransaction(@NonNull Map<InstanceIdentifier, DataObject> cfgData, @NonNull Map<InstanceIdentifier, DataObject> operData){
        rwtransaction = new SealedReadWriteTransaction(cfgData, operData);
    }

    @NonNull
    @Override
    public Object getIdentifier() {
        return rwtransaction.getIdentifier();
    }

    @Override
    public boolean cancel() {
        return rwtransaction.cancel();
    }

    @Override
    public @NonNull FluentFuture<? extends CommitInfo> commit() {
        System.out.println("w tx commit");return rwtransaction.commit();
    }

    @Override
    public <T extends DataObject> void put(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {
        rwtransaction.put(store, path, data);
    }

    @Override
    public <T extends DataObject> void mergeParentStructurePut(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {
        rwtransaction.mergeParentStructurePut(store, path, data);
    }

    @Override
    public <T extends DataObject> void merge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {
        rwtransaction.merge(store, path, data);
    }

    @Override
    public <T extends DataObject> void mergeParentStructureMerge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {
        rwtransaction.mergeParentStructurePut(store, path, data);
    }

    @Override
    public void delete(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<?> path) {
        rwtransaction.delete(store, path);
    }
}

class SealedReadWriteTransaction implements ReadWriteTransaction{

    private volatile Map<InstanceIdentifier, DataObject> operData;
    private volatile Map<InstanceIdentifier, DataObject> cfgData;
    private volatile UUID id;

    SealedReadWriteTransaction(@NonNull Map<InstanceIdentifier, DataObject> cfgData, @NonNull Map<InstanceIdentifier, DataObject> operData){
        this.cfgData = cfgData;
        this.operData = operData;
        this.id = UUID.randomUUID();
    }

    @Override
    public @NonNull <T extends DataObject> FluentFuture<Optional<T>> read(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path) {
        switch (store){
            case OPERATIONAL:
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
                                return operData.get(path);
                            }

                            @Override
                            public Object get(long timeout, TimeUnit unit)
                                    throws InterruptedException, ExecutionException, TimeoutException {
                                return operData.get(path);
                            }

                            @Override
                            public void addListener(Runnable runnable, Executor executor) {}
                        });
            case CONFIGURATION:
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
                                return cfgData.get(path);
                            }

                            @Override
                            public Object get(long timeout, TimeUnit unit)
                                    throws InterruptedException, ExecutionException, TimeoutException {
                                return cfgData.get(path);
                            }

                            @Override
                            public void addListener(Runnable runnable, Executor executor) {}
                        });

        }
        throw new RuntimeException("invalid data store type");
    }

    @Override
    public @NonNull FluentFuture<Boolean> exists(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<?> path) {
        return FluentFuture.from(new ListenableFuture() {
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

    @NonNull
    @Override
    public Object getIdentifier() {
        return id;
    }

    @Override
    public boolean cancel() {
        this.operData = null;
        this.cfgData = null;
        this.id = null;
        return true;
    }

    @Override
    public @NonNull FluentFuture<? extends CommitInfo> commit() {
        System.out.println("rw tx commit");
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
                        return CommitInfo.emptyFluentFuture();
                    }

                    @Override
                    public Object get(long timeout, TimeUnit unit)
                            throws InterruptedException, ExecutionException, TimeoutException {
                        return this.get();
                    }

                    @Override
                    public void addListener(Runnable runnable, Executor executor) {}
                });
    }

    @Override
    public <T extends DataObject> void put(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {

        System.out.println(store);
        switch (store){
            case OPERATIONAL:
                this.operData.put(path, data);
                return;
            case CONFIGURATION:
                this.cfgData.put(path, data);
                return;
            default:
                throw new RuntimeException("invalid data store type");
        }

    }

    @Override
    public <T extends DataObject> void mergeParentStructurePut(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {
        this.put(store, path, data);
    }

    @Override
    public <T extends DataObject> void merge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {
        this.put(store, path, data);
    }

    @Override
    public <T extends DataObject> void mergeParentStructureMerge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {
        this.put(store, path, data);
    }

    @Override
    public void delete(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<?> path) {
        switch (store){
            case OPERATIONAL:
                this.operData.remove(path);
            case CONFIGURATION:
                this.cfgData.remove(path);
        }
        throw new RuntimeException("invalid data store type");
    }
}
