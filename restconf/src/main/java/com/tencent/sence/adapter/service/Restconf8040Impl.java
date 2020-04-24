/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sence.adapter.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.tencent.sence.adapter.seal.*;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.*;
import org.opendaylight.mdsal.dom.broker.DOMMountPointServiceImpl;
import org.opendaylight.mdsal.dom.broker.schema.ScanningSchemaServiceProvider;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.restconf.nb.rfc8040.handlers.*;
import org.opendaylight.restconf.nb.rfc8040.rests.services.api.JSONRestconfService;
import org.opendaylight.restconf.nb.rfc8040.rests.services.impl.JSONRestconfServiceRfc8040Impl;
import org.opendaylight.restconf.nb.rfc8040.services.wrapper.ServicesWrapper;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Restconf8040Impl {

  JSONRestconfService service;

  private SchemaContext schemaContext;

  private ScanningSchemaServiceProvider domSchemaService = new SealedDOMSchemaService();
  private ImmutableMap<LogicalDatastoreType, DOMStore> dataStore =
      SealedDataStore.getDomDataStore(domSchemaService);
  private DOMDataBroker dataBroker = new SealedDOMDataBroker(dataStore);

  private DOMRpcService rpcService = new SealedRpcService();
  private DOMActionService actionService = new SealedActionService();

  private DOMTransactionChain transactionChain = SealedDOMTransactionChain.getInstance();
  private DOMMountPointService mountPointService = new DOMMountPointServiceImpl();

  private static ArrayList<URL> yangs = new ArrayList<>();

  public void init() {
    yangs.add(
        ScanningSchemaServiceProvider.class.getResource(
            "/modules/ietf-interfaces@2013-07-04.yang"));
    yangs.forEach(System.out::println);
    domSchemaService.registerAvailableYangs(yangs);
    final TransactionChainHandler txChainHandler = new TransactionChainHandler(dataBroker);

    schemaContext = YangParserTestUtils.parseYangResourceDirectory("/modules");
    schemaContext.getModules().forEach(System.out::println);
    SchemaContextHandler schemaContextHandler =
        new SchemaContextHandler(txChainHandler, domSchemaService);
    schemaContextHandler.onGlobalContextUpdated(schemaContext);

    final DOMMountPointServiceHandler mountPointServiceHandler =
        new DOMMountPointServiceHandler(mountPointService);
    final DOMNotificationService notificationService = null;

    final ServicesWrapper servicesWrapper =
        ServicesWrapper.newInstance(
            schemaContextHandler,
            mountPointServiceHandler,
            txChainHandler,
            new DOMDataBrokerHandler(dataBroker),
            new RpcServiceHandler(rpcService),
            new ActionServiceHandler(actionService),
            new NotificationServiceHandler(notificationService),
            domSchemaService);

    service =
        new JSONRestconfServiceRfc8040Impl(
            servicesWrapper, mountPointServiceHandler, schemaContextHandler);
  }

  private String loadData(final String path) throws IOException {
    return Resources.asCharSource(Restconf8040Impl.class.getResource(path), StandardCharsets.UTF_8)
        .read();
  }

  public void put() throws Exception {
    final String uriPath = "ietf-interfaces:interfaces/interface=eth0";
    final String payload = loadData("/parts/ietf-interfaces_interfaces.json");

    this.service.put(uriPath, payload);
    System.out.println(this.dataStore.get(LogicalDatastoreType.OPERATIONAL));
  }

  public static void main(String[] s) throws Exception {
    Restconf8040Impl svc = new Restconf8040Impl();
    svc.init();
    svc.put();
  }
}
