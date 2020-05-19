package com.tencent.sense.adapter.core;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.*;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;

import java.util.HashMap;
import java.util.Map;

public class SealedDataBroker implements DataBroker {

    private SealedTransactionChain txChain = new SealedTransactionChain();
    private static final Map<DataTreeIdentifier, DataTreeChangeListener> listenerReg = new HashMap<DataTreeIdentifier, DataTreeChangeListener>();

    @Override
    public @NonNull TransactionChain createTransactionChain(@NonNull TransactionChainListener listener) {
        return txChain;
    }

    @Override
    public @NonNull TransactionChain createMergingTransactionChain(@NonNull TransactionChainListener listener) {
        return txChain;
    }

    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(@NonNull DataTreeIdentifier<T> treeId, @NonNull L listener) {
        listenerReg.putIfAbsent(treeId, listener);
        return null;
    }

    @Override
    public @NonNull ReadTransaction newReadOnlyTransaction() {
        return txChain.newReadOnlyTransaction();
    }

    @Override
    public @NonNull ReadWriteTransaction newReadWriteTransaction() {
        return txChain.newReadWriteTransaction();
    }

    @Override
    public @NonNull WriteTransaction newWriteOnlyTransaction() {
        return txChain.newWriteOnlyTransaction();
    }
}
