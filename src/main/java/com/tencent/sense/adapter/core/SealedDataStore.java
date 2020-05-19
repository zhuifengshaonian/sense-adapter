/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sense.adapter.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class SealedDataStore {

  private DOMSchemaService schemaService;
  private static ImmutableMap<LogicalDatastoreType, DOMStore> store;

  public static ImmutableMap<LogicalDatastoreType, DOMStore> getDomDataStore(
      DOMSchemaService schemaService) {
    if (store == null) store = new com.tencent.sense.adapter.core.SealedDataStore().createDatastores(schemaService);

    return store;
  }

  public ImmutableMap<LogicalDatastoreType, DOMStore> createDatastores(
      DOMSchemaService schemaService) {
    this.schemaService = schemaService;
    return ImmutableMap.<LogicalDatastoreType, DOMStore>builder()
        .put(LogicalDatastoreType.OPERATIONAL, createOperationalDatastore())
        .put(LogicalDatastoreType.CONFIGURATION, createConfigurationDatastore())
        .build();
  }

  private SchemaContext schemaContext = YangParserTestUtils.parseYangResourceDirectory("/modules");

  private DOMStore createConfigurationDatastore() {
    final InMemoryDOMDataStore store =
        new InMemoryDOMDataStore("CFG", getDataTreeChangeListenerExecutor());
    store.onGlobalContextUpdated(schemaContext);
    schemaService.registerSchemaContextListener(store);
    return store;
  }

  private DOMStore createOperationalDatastore() {
    final InMemoryDOMDataStore store =
        new InMemoryDOMDataStore("OPER", getDataTreeChangeListenerExecutor());
    store.onGlobalContextUpdated(schemaContext);
    schemaService.registerSchemaContextListener(store);
    return store;
  }

  private ListeningExecutorService getDataTreeChangeListenerExecutor() {
    return MoreExecutors.newDirectExecutorService();
  }
}
