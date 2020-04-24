package com.tencent.sense.adapter.netconf;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.netconf.api.NetconfMessage;
import org.opendaylight.netconf.api.messages.NetconfHelloMessageAdditionalHeader;
import org.opendaylight.netconf.api.xml.XmlUtil;
import org.opendaylight.netconf.impl.NetconfServerSessionListener;
import org.opendaylight.netconf.topology.impl.NetconfTopologyImpl;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.w3c.dom.Document;
import org.opendaylight.netconf.impl.NetconfServerSession;

import java.util.ArrayList;

public class NcClient {

    private static final String HOST = "127.0.0.1";
    private static final String PORT = "17830";
    private static final String SSH_TRANSPORT = "ssh";
    private static final String TCP_TRANSPORT = "tcp";
    private static final String SESSION_ID = "1";
    private static final String USER = "admin";
    private NetconfServerSession session;
    private EmbeddedChannel channel;
    private NetconfMessage msg;
    private NetconfServerSessionListener listener;

    public void setUp() throws Exception {
        final NetconfHelloMessageAdditionalHeader header =
                new NetconfHelloMessageAdditionalHeader(USER, HOST, PORT, SSH_TRANSPORT, SESSION_ID);
        channel = new EmbeddedChannel();
        session = new NetconfServerSession(listener, channel, 1L, header);
        msg = new NetconfMessage(XmlUtil.readXmlToDocument("<rpc-reply></rpc-reply>"));
    }

//    public void testSessionUp() throws Exception {
//        session.sessionUp();
//        verify(listener).onSessionUp(session);
//    }
//
//    public void testDelayedClose() throws Exception {
//        doNothing().when(listener).onSessionTerminated(eq(session), any());
//        session.delayedClose();
//        session.sendMessage(msg);
//        channel.runPendingTasks();
//        final Object o = channel.readOutbound();
//        Assert.assertEquals(msg, o);
//        verify(listener).onSessionTerminated(eq(session), any());
//    }
//
    public void testSendMessage() throws Exception {
        session.sendMessage(msg);
        channel.runPendingTasks();
        final Object o = channel.readOutbound();
        System.out.println(o);
        System.out.println(o.equals(msg));
        System.out.println(o.hashCode() == msg.hashCode());
    }

    public static void main(String[] s){
        try{
            NcClient ncc = new NcClient();
            ncc.setUp();
            ncc.testSendMessage();

            System.out.println("xxxxxxxxxxxxxxx");

            NetconfTopologyImpl ncTopo = new NetconfTopologyImpl(null, null, null, null, null,
                    null, null, null, null, null);

            ArrayList<DataTreeModification<Node>> nodes = new ArrayList<DataTreeModification<Node>>();
            nodes.add(new DataTreeModification<Node>() {
                @Override
                public @NonNull DataTreeIdentifier<Node> getRootPath() {
                    return null;
                }

                @Override
                public @NonNull DataObjectModification<Node> getRootNode() {
                    return null;
                }
            });

            ncTopo.onDataTreeChanged(nodes);

        }catch (Exception e){
            System.out.println(e);
        }
    }
//
//    public void testSendNotification() throws Exception {
//        doNothing().when(listener).onNotification(any(), any());
//        final Document msgDoc = XmlUtil.readXmlToDocument("<notification></notification>");
//        final NetconfNotification notif = new NetconfNotification(msgDoc);
//        session.sendMessage(notif);
//        channel.runPendingTasks();
//        final Object o = channel.readOutbound();
//        Assert.assertEquals(notif, o);
//        verify(listener).onNotification(session, notif);
//    }
//
//    public void testOnIncommingRpcSuccess() throws Exception {
//        session.sessionUp();
//        final Session managementSession = this.session.toManagementSession();
//        this.session.onIncommingRpcSuccess();
//        final Session afterRpcSuccess = this.session.toManagementSession();
//        Assert.assertEquals(managementSession.getInRpcs().getValue().toJava() + 1,
//                afterRpcSuccess.getInRpcs().getValue().longValue());
//    }
//
//    @Test
//    public void testOnIncommingRpcFail() throws Exception {
//        session.sessionUp();
//        final Session managementSession = this.session.toManagementSession();
//        this.session.onIncommingRpcFail();
//        final Session afterRpcSuccess = this.session.toManagementSession();
//        Assert.assertEquals(managementSession.getInBadRpcs().getValue().toJava() + 1,
//                afterRpcSuccess.getInBadRpcs().getValue().longValue());
//    }
//
//    public void testOnOutgoingRpcError() throws Exception {
//        session.sessionUp();
//        final Session managementSession = this.session.toManagementSession();
//        this.session.onOutgoingRpcError();
//        final Session afterRpcSuccess = this.session.toManagementSession();
//        Assert.assertEquals(managementSession.getOutRpcErrors().getValue().toJava() + 1,
//                afterRpcSuccess.getOutRpcErrors().getValue().longValue());
//    }
//
//    public void testToManagementSession() throws Exception {
//        final NetconfHelloMessageAdditionalHeader header =
//                new NetconfHelloMessageAdditionalHeader(USER, HOST, PORT, TCP_TRANSPORT, SESSION_ID);
//        final EmbeddedChannel ch = new EmbeddedChannel();
//        final NetconfServerSession tcpSession = new NetconfServerSession(listener, ch, 1L, header);
//        tcpSession.sessionUp();
//        final Session managementSession = tcpSession.toManagementSession();
//        Assert.assertEquals(HOST, managementSession.getSourceHost().getIpAddress().getIpv4Address().getValue());
//        Assert.assertEquals(managementSession.getUsername(), USER);
//        Assert.assertEquals(managementSession.getSessionId().toString(), SESSION_ID);
//        Assert.assertEquals(managementSession.getTransport(), NetconfTcp.class);
//    }
//
//    public void testToManagementSessionUnknownTransport() throws Exception {
//        final NetconfHelloMessageAdditionalHeader header =
//                new NetconfHelloMessageAdditionalHeader(USER, HOST, PORT, "http", SESSION_ID);
//        final EmbeddedChannel ch = new EmbeddedChannel();
//        final NetconfServerSession tcpSession = new NetconfServerSession(listener, ch, 1L, header);
//        tcpSession.sessionUp();
//        tcpSession.toManagementSession();
//        tcpSession.close();
//    }
//
//    public void testToManagementSessionIpv6() throws Exception {
//        final NetconfHelloMessageAdditionalHeader header =
//                new NetconfHelloMessageAdditionalHeader(USER, "::1", PORT, SSH_TRANSPORT, SESSION_ID);
//        final EmbeddedChannel ch = new EmbeddedChannel();
//        final NetconfServerSession tcpSession = new NetconfServerSession(listener, ch, 1L, header);
//        tcpSession.sessionUp();
//        final Session managementSession = tcpSession.toManagementSession();
//        Assert.assertEquals("::1", managementSession.getSourceHost().getIpAddress().getIpv6Address().getValue());
//        Assert.assertEquals(managementSession.getUsername(), USER);
//        Assert.assertEquals(managementSession.getSessionId().toString(), SESSION_ID);
//        Assert.assertEquals(managementSession.getTransport(), NetconfSsh.class);
//    }
//
//    public void testThisInstance() throws Exception {
//        Assert.assertEquals(session, session.thisInstance());
//    }
//
//    public void testAddExiHandlers() throws Exception {
//        channel.pipeline().addLast(AbstractChannelInitializer.NETCONF_MESSAGE_DECODER,
//                new NetconfXMLToMessageDecoder());
//        channel.pipeline().addLast(AbstractChannelInitializer.NETCONF_MESSAGE_ENCODER,
//                new NetconfMessageToXMLEncoder());
//        final NetconfEXICodec codec = NetconfEXICodec.forParameters(EXIParameters.empty());
//        session.addExiHandlers(NetconfEXIToMessageDecoder.create(codec), NetconfMessageToEXIEncoder.create(codec));
//    }
//
//    public void testStopExiCommunication() throws Exception {
//        channel.pipeline().addLast(AbstractChannelInitializer.NETCONF_MESSAGE_DECODER,
//                new ChannelInboundHandlerAdapter());
//        channel.pipeline().addLast(AbstractChannelInitializer.NETCONF_MESSAGE_ENCODER,
//                new ChannelOutboundHandlerAdapter());
//        session.stopExiCommunication();
//        //handler is replaced only after next send message call
//        final ChannelHandler exiEncoder = channel.pipeline().get(AbstractChannelInitializer.NETCONF_MESSAGE_ENCODER);
//        Assert.assertTrue(ChannelOutboundHandlerAdapter.class.equals(exiEncoder.getClass()));
//        session.sendMessage(msg);
//        channel.runPendingTasks();
//        final ChannelHandler decoder = channel.pipeline().get(AbstractChannelInitializer.NETCONF_MESSAGE_DECODER);
//        Assert.assertTrue(NetconfXMLToMessageDecoder.class.equals(decoder.getClass()));
//        final ChannelHandler encoder = channel.pipeline().get(AbstractChannelInitializer.NETCONF_MESSAGE_ENCODER);
//        Assert.assertTrue(NetconfMessageToXMLEncoder.class.equals(encoder.getClass()));
//    }

}
