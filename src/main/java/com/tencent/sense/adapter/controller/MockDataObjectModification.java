package com.tencent.sense.adapter.controller;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.Collection;

public class MockDataObjectModification implements DataObjectModification {

    private InstanceIdentifier.PathArgument pa;
    @Override
    public InstanceIdentifier.PathArgument getIdentifier() {
        return pa;
    }

    public void setIdentifier(InstanceIdentifier.PathArgument pa){
        this.pa = pa;
    }

    @Override
    public @NonNull Class getDataType() {
        return null;
    }

    @Override
    public @NonNull ModificationType getModificationType() {
        return DataObjectModification.ModificationType.WRITE;
    }

    @Nullable
    @Override
    public DataObject getDataBefore() {
        return null;
    }
    DataObject data;
    @Nullable
    @Override
    public DataObject getDataAfter() {
        return data;
    }

    public void setDataAfter(DataObject data){
        this.data = data;
    }

    @Override
    public @NonNull Collection<? extends DataObjectModification<? extends DataObject>> getModifiedChildren() {
        return null;
    }

    @Override
    public @Nullable DataObjectModification<? extends DataObject> getModifiedChild(InstanceIdentifier.PathArgument childArgument) {
        return null;
    }

    @Override
    public @Nullable DataObjectModification getModifiedChildListItem(@NonNull Class caseType, @NonNull Class listItem, @NonNull Identifier listKey) {
        return null;
    }

    @Override
    public @Nullable DataObjectModification getModifiedChildListItem(@NonNull Class listItem, @NonNull Identifier listKey) {
        return null;
    }

    @Override
    public @Nullable DataObjectModification getModifiedAugmentation(@NonNull Class augmentation) {
        return null;
    }

    @Override
    public @Nullable DataObjectModification getModifiedChildContainer(@NonNull Class child) {
        return null;
    }

    @Override
    public @Nullable DataObjectModification getModifiedChildContainer(@NonNull Class caseType, @NonNull Class child) {
        return null;
    }

    @Override
    public Collection<DataObjectModification> getModifiedChildren(@NonNull Class caseType, @NonNull Class childType) {
        return null;
    }

    @Override
    public Collection<DataObjectModification> getModifiedChildren(@NonNull Class childType) {
        return null;
    }
}
