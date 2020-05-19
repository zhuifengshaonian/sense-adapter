package com.tencent.sense.adapter.controller;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;

public class MockDataTreeModification implements DataTreeModification {

    private DataObjectModification rootNode;
    @Override
    public @NonNull DataTreeIdentifier getRootPath() {
        return null;
    }


    public void setRootNode(DataObjectModification rootNode){
        this.rootNode = rootNode;
    }
    @Override
    public @NonNull DataObjectModification getRootNode() {
        return rootNode;
    }
}
