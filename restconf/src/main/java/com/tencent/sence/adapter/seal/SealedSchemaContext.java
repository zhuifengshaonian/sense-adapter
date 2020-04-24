/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sence.adapter.seal;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.*;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class SealedSchemaContext implements SchemaContext {

  @Override
  public Set<DataSchemaNode> getDataDefinitions() {
    return null;
  }

  @Override
  public Set<Module> getModules() {
    return null;
  }

  @Override
  public Set<RpcDefinition> getOperations() {
    return null;
  }

  @Override
  public Set<ExtensionDefinition> getExtensions() {
    return null;
  }

  @Override
  public Optional<Module> findModule(@NonNull QNameModule qnameModule) {
    return Optional.empty();
  }

  @Override
  public boolean isPresenceContainer() {
    return false;
  }

  @Override
  public Set<AugmentationSchemaNode> getAvailableAugmentations() {
    return null;
  }

  @Override
  public boolean isAugmenting() {
    return false;
  }

  @Override
  public boolean isAddedByUses() {
    return false;
  }

  @Override
  public Set<TypeDefinition<?>> getTypeDefinitions() {
    return null;
  }

  @Override
  public Collection<DataSchemaNode> getChildNodes() {
    return null;
  }

  @Override
  public Set<GroupingDefinition> getGroupings() {
    return null;
  }

  @Override
  public Optional<DataSchemaNode> findDataChildByName(QName name) {
    return Optional.empty();
  }

  @Override
  public Set<UsesNode> getUses() {
    return null;
  }

  @Override
  public boolean isConfiguration() {
    return false;
  }

  @Override
  public @NonNull Set<NotificationDefinition> getNotifications() {
    return null;
  }

  @Override
  public @NonNull QName getQName() {
    return null;
  }

  @Override
  public @NonNull SchemaPath getPath() {
    return null;
  }

  @Override
  public @NonNull Status getStatus() {
    return null;
  }
}
