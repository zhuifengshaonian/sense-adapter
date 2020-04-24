/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.tencent.sence.adapter;

import com.google.common.collect.ImmutableSet;
import com.tencent.sence.adapter.seal.SealedDOMDataBroker;
import com.tencent.sence.adapter.seal.SealedDOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointListener;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.restconf.common.context.NormalizedNodeContext;
import org.opendaylight.restconf.common.schema.SchemaExportContext;
import org.opendaylight.restconf.nb.rfc8040.handlers.DOMMountPointServiceHandler;
import org.opendaylight.restconf.nb.rfc8040.handlers.SchemaContextHandler;
import org.opendaylight.restconf.nb.rfc8040.handlers.TransactionChainHandler;
import org.opendaylight.restconf.nb.rfc8040.services.simple.api.RestconfSchemaService;
import org.opendaylight.restconf.nb.rfc8040.services.simple.impl.RestconfImpl;
import org.opendaylight.restconf.nb.rfc8040.services.simple.impl.RestconfOperationsServiceImpl;
import org.opendaylight.restconf.nb.rfc8040.services.simple.impl.RestconfSchemaServiceImpl;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

import javax.ws.rs.core.UriInfo;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Optional;
import java.util.Set;

public class OperantionService {

  private static void simpleInit() throws FileNotFoundException {
    final SchemaContext schemaContext =
        YangParserTestUtils.parseYangFiles(TestRestconfUtils.loadFiles("/restconf/impl"));
    final SchemaContextHandler schemaContextHandler =
        TestUtils.newSchemaContextHandler(schemaContext);
    final RestconfImpl restconfImpl = new RestconfImpl(schemaContextHandler);
    final NormalizedNodeContext libraryVersion = restconfImpl.getLibraryVersion();
    final LeafNode<?> value = (LeafNode<?>) libraryVersion.getData();

    System.out.println(value.getValue());

    schemaContext.getModules().forEach(System.out::println);
  }

  private static DOMMountPointService domMountPointService =
      new DOMMountPointService() {
        @Override
        public Optional<DOMMountPoint> getMountPoint(YangInstanceIdentifier path) {
          return Optional.empty();
        }

        @Override
        public DOMMountPointBuilder createMountPoint(YangInstanceIdentifier path) {
          return null;
        }

        @Override
        public ListenerRegistration<DOMMountPointListener> registerProvisionListener(
            DOMMountPointListener listener) {
          return null;
        }
      };

  private static SchemaContext schemaContext;
  private static SchemaContextHandler schemaContextHandler;
  private static DOMMountPointServiceHandler domMountPointServiceHandler;
  private static Set<QName> listOfRpcsNames;
  private static UriInfo uriInfo;

  public static void restconfOperationService() throws FileNotFoundException {
    schemaContext = YangParserTestUtils.parseYangFiles(TestRestconfUtils.loadFiles("/modules"));
    schemaContextHandler = TestUtils.newSchemaContextHandler(schemaContext);

    domMountPointServiceHandler = DOMMountPointServiceHandler.newInstance(domMountPointService);

    final QNameModule module1 = QNameModule.create(URI.create("module:1"));
    final QNameModule module2 = QNameModule.create(URI.create("module:2"));

    listOfRpcsNames =
        ImmutableSet.of(
            QName.create(module1, "dummy-rpc1-module1"),
            QName.create(module1, "dummy-rpc2-module1"),
            QName.create(module2, "dummy-rpc1-module2"),
            QName.create(module2, "dummy-rpc2-module2"));

    final RestconfOperationsServiceImpl oper =
        new RestconfOperationsServiceImpl(schemaContextHandler, domMountPointServiceHandler);
    final NormalizedNodeContext operations = oper.getOperations(uriInfo);
    final ContainerNode data = (ContainerNode) operations.getData();
    System.out.println(data.getNodeType().getNamespace().toString());
    System.out.println(
        "urn:ietf:params:xml:ns:yang:ietf-restconf"
            .equals(data.getNodeType().getNamespace().toString()));

    System.out.println(data.getNodeType().getLocalName());
    System.out.println("operations".equals(data.getNodeType().getLocalName()));

    for (final DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> child :
        data.getValue()) {
      System.out.println(Empty.getInstance().equals(child.getValue()));

      final QName qname = child.getNodeType().withoutRevision();
      System.out.println(listOfRpcsNames.contains(qname));
    }
  }

  private static RestconfSchemaService schemaService;
  private static SchemaContext schemaContextBehindMountPoint;
  private static DOMMountPointService mountPointService;

  private static void restconfMountpoint() throws FileNotFoundException {
    schemaContext = YangParserTestUtils.parseYangFiles(TestRestconfUtils.loadFiles("/modules"));
    schemaContextBehindMountPoint =
        YangParserTestUtils.parseYangFiles(
            TestRestconfUtils.loadFiles("/modules/modules-behind-mount-point"));

    System.out.println(schemaContext);
    System.out.println(schemaContextBehindMountPoint);

    mountPointService = new DOMMountPointServiceImpl();
    // create and register mount points
    mountPointService
        .createMountPoint(
            YangInstanceIdentifier.of(QName.create("mount:point:1", "2016-01-01", "cont")))
        .addInitialSchemaContext(schemaContextBehindMountPoint)
        .register();
    mountPointService
        .createMountPoint(
            YangInstanceIdentifier.of(QName.create("mount:point:2", "2016-01-01", "cont")))
        .register();

    System.out.println(
        mountPointService
            .getMountPoint(
                YangInstanceIdentifier.of(QName.create("mount:point:1", "2016-01-01", "cont")))
            .get()
            .getSchemaContext());
    System.out.println("xxxxxxxxxxx");

    TransactionChainHandler transactionChainHandler =
        new TransactionChainHandler(new SealedDOMDataBroker(null));
    SchemaContextHandler contextHandler =
        new SchemaContextHandler(transactionChainHandler, new SealedDOMSchemaService());
    contextHandler.onGlobalContextUpdated(schemaContext);
    schemaService =
        new RestconfSchemaServiceImpl(
            contextHandler, DOMMountPointServiceHandler.newInstance(mountPointService), null);

    final SchemaExportContext exportContext = schemaService.getSchema("module1/2014-01-01");

    System.out.println(exportContext);

    final Module module = exportContext.getModule();

    System.out.println(module.getName());
    System.out.println(module.getRevision());
    System.out.println(module.getNamespace().toString());
  }

  public static void main(String[] s) throws FileNotFoundException {

    restconfOperationService();

    restconfMountpoint();
  }
}
