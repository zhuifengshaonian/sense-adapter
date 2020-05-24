package com.tencent.sense.adapter.controller;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tencent.sense.adapter.core.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.bouncycastle.util.Arrays;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.controller.config.threadpool.ScheduledThreadPool;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.netconf.api.messages.NetconfHelloMessageAdditionalHeader;
import org.opendaylight.netconf.client.*;
import org.opendaylight.netconf.client.conf.NetconfClientConfiguration;
import org.opendaylight.netconf.client.conf.NetconfReconnectingClientConfiguration;
import org.opendaylight.netconf.client.conf.NetconfReconnectingClientConfigurationBuilder;
import org.opendaylight.netconf.nettyutil.ReconnectStrategy;
import org.opendaylight.netconf.nettyutil.ReconnectStrategyFactory;
import org.opendaylight.netconf.nettyutil.TimedReconnectStrategyFactory;
import org.opendaylight.netconf.nettyutil.handler.ssh.authentication.AuthenticationHandler;
import org.opendaylight.netconf.nettyutil.handler.ssh.authentication.LoginPasswordHandler;
import org.opendaylight.netconf.sal.connect.netconf.listener.NetconfDeviceCapabilities;
import org.opendaylight.netconf.topology.api.NetconfTopology;
import org.opendaylight.netconf.topology.impl.NetconfTopologyImpl;
import org.opendaylight.netconf.topology.impl.SchemaRepositoryProviderImpl;
import org.opendaylight.netconf.topology.impl.TopologyUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.springframework.stereotype.Controller;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.*;

import org.opendaylight.mdsal.dom.broker.DOMMountPointServiceImpl;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.SAXException;

@Controller
public class NcTopologyController {
    private static final String TOPOLOGY_ID = "netconf-topology";
    private static final NodeId NODE_ID = new NodeId("ne-node");

    private static NetconfTopologyImpl topology;

    private static final NetconfClientDispatcher netconfClientDispatcher =
            new NetconfClientDispatcherImpl(new NioEventLoopGroup(8), new NioEventLoopGroup(8), new HashedWheelTimer());

    private class ScheduledThreadPoolWrapper implements ScheduledThreadPool, Closeable {

        private final ScheduledThreadPoolExecutor executor;
        private final int threadCount;

        public ScheduledThreadPoolWrapper(int threadCount, ThreadFactory factory) {
            this.threadCount = threadCount;
            this.executor = new ScheduledThreadPoolExecutor(threadCount, factory);
            executor.prestartAllCoreThreads();
        }

        @Override
        public ScheduledExecutorService getExecutor() {
            return Executors.unconfigurableScheduledExecutorService(executor);
        }

        @Override
        public void close() {
            executor.shutdown();
        }

        @Override
        public int getMaxThreadCount() {
            return threadCount;
        }

    }

    private static DOMMountPointService domMountPointService = new SealedDOMMountPointService();

    public NcTopologyController(){
        System.out.println("xxxxxxxxxxxxxxxxx.............xxxxxxxxxxxxxxxxxxxxx");
        topology =
        new NetconfTopologyImpl(
            TOPOLOGY_ID,
            netconfClientDispatcher,
            GlobalEventExecutor.INSTANCE,
            new ScheduledThreadPoolWrapper(8, new ThreadFactoryBuilder().build()),
            new ScheduledThreadPoolWrapper(8, new ThreadFactoryBuilder().build()),
            new SchemaRepositoryProviderImpl("default"),
            new SealedDataBroker(), domMountPointService,
            new AAAEncryptionService() {
              @Override
              public String encrypt(String data) {
                return null;
              }

              @Override
              public byte[] encrypt(byte[] data) {
                return Arrays.copyOf(data, data.length);
              }

              @Override
              public String decrypt(String encryptedData) {
                return new String(encryptedData);
              }

              @Override
              public byte[] decrypt(byte[] encryptedData) {
                  return Arrays.copyOf(encryptedData, encryptedData.length);
              }
            });

        final InstanceIdentifier<NetworkTopology> networkTopologyId =
                InstanceIdentifier.builder(NetworkTopology.class).build();
        final Topology topo = new TopologyBuilder().setTopologyId(new TopologyId(TOPOLOGY_ID)).build();
        final NetworkTopology networkTopology = new NetworkTopologyBuilder().build();

        WriteTransaction wtx = new WriteTransaction() {
            @NonNull
            @Override
            public Object getIdentifier() {
                return null;
            }

            @Override
            public <T extends DataObject> void put(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {

            }

            @Override
            public <T extends DataObject> void mergeParentStructurePut(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {

            }

            @Override
            public <T extends DataObject> void merge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {
                System.out.println("merge: "+path);
            }

            @Override
            public <T extends DataObject> void mergeParentStructureMerge(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path, @NonNull T data) {

            }

            @Override
            public void delete(@NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<?> path) {

            }

            @Override
            public boolean cancel() {
                return false;
            }

            @Override
            public @NonNull FluentFuture<? extends CommitInfo> commit() {
                return null;
            }
        };
        wtx.merge(LogicalDatastoreType.CONFIGURATION, networkTopologyId, networkTopology);
        wtx.merge(LogicalDatastoreType.OPERATIONAL, networkTopologyId, networkTopology);
        wtx.merge(LogicalDatastoreType.CONFIGURATION,
                networkTopologyId.child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_ID))), topo);
        wtx.merge(LogicalDatastoreType.OPERATIONAL,
                networkTopologyId.child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_ID))), topo);


    }

    @RequestMapping("/hello")
    public void testOnDataTreeChange() throws InterruptedException, SAXException, IOException, URISyntaxException {

        final DataObjectModification<Node> newNode = new MockDataObjectModification();
        InstanceIdentifier.PathArgument pa = null;

        for (final InstanceIdentifier.PathArgument p
                : TopologyUtil.createTopologyListPath(TOPOLOGY_ID)
                .child(Node.class, new NodeKey(NODE_ID)).getPathArguments()) {
            pa = p;
        }

        ((MockDataObjectModification) newNode).setIdentifier(pa);


        final NetconfNode testingNode = new NetconfNodeBuilder()
                .setHost(new Host(new IpAddress(new Ipv4Address("127.0.0.1"))))
                .setPort(new PortNumber(Uint16.valueOf(17830)))
                .setReconnectOnChangedSchema(true)
                .setDefaultRequestTimeoutMillis(Uint32.valueOf(1000))
                .setBetweenAttemptsTimeoutMillis(Uint16.valueOf(100))
                .setKeepaliveDelay(Uint32.valueOf(1000))
                .setSchemaless(false)
                .setCredentials(new LoginPasswordBuilder()
                        .setUsername("testuser").setPassword("testpassword").build())
                .build();

        final NodeBuilder nn = new NodeBuilder().addAugmentation(NetconfNode.class, testingNode);

        ((MockDataObjectModification) newNode).setDataAfter(nn.build());

        final Collection<DataTreeModification<Node>> changes = new HashSet<>();
        final DataTreeModification<Node> ch = new MockDataTreeModification();
        ((MockDataTreeModification) ch).setRootNode(newNode);

        changes.add(ch);
        topology.onDataTreeChanged(changes);

        Thread.sleep(30000L);
        System.out.println("finish conn to: "+newNode);

        DOMMountPoint mp = ((SealedDOMMountPointService)domMountPointService).getMountPoint("ne-node").get();
        SchemaContext schemaContext = mp.getSchemaContext();
        System.out.println(schemaContext);

        final NormalizedNode normalizedNode = new YangTest().jsonToNormalizedNodes(schemaContext);
        System.out.println("decode json to: "+normalizedNode);
        Thread.sleep(60000L);

    }

    public void testNetconfClientDispatcherImpl() throws Exception {

        Long timeout = 200L;
        NetconfHelloMessageAdditionalHeader header =
            new NetconfHelloMessageAdditionalHeader("user", "127.0.0.1", "17830", "trans", "12345");
        NetconfClientSessionListener listener = new SimpleNetconfClientSessionListener();
        InetSocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 17830);
    ReconnectStrategyFactory reconnectStrategyFactory =
        new TimedReconnectStrategyFactory(GlobalEventExecutor.INSTANCE, 5000L, 2, BigDecimal.ONE);
        AuthenticationHandler handler = new LoginPasswordHandler("user", "password");
        ReconnectStrategy reconnect = new ReconnectStrategy() {
            @Override
            public int getConnectTimeout() throws Exception {
                return 2;
            }

            @Override
            public io.netty.util.concurrent.Future<Void> scheduleReconnect(Throwable cause) {
                return null;
            }

            @Override
            public void reconnectSuccessful() {

            }
        };

        NetconfReconnectingClientConfiguration cfg =
            NetconfReconnectingClientConfigurationBuilder.create()
                .withProtocol(NetconfClientConfiguration.NetconfClientProtocol.SSH)
                .withAddress(address)
                .withConnectionTimeoutMillis(timeout)
                .withReconnectStrategy(reconnect)
                .withAdditionalHeader(header)
                .withSessionListener(listener)
                .withConnectStrategyFactory(reconnectStrategyFactory)
                .withAuthHandler(handler)
                .build();

        NetconfClientDispatcherImpl dispatcher =
            new NetconfClientDispatcherImpl(new NioEventLoopGroup(8), new NioEventLoopGroup(8), new HashedWheelTimer());

        Future<Void> sshReconn = dispatcher.createReconnectingClient(cfg);

        System.out.println(sshReconn.get());
    }
}
