package com.tencent.sense.adapter.controller;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.codec.xml.XMLStreamNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.opendaylight.restconf.common.context.InstanceIdentifierContext;

import org.opendaylight.restconf.nb.rfc8040.utils.parser.IdentifierCodec;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class YangTest {

    private static String loadTextFile(String relativePath) throws IOException, URISyntaxException {
        File file = new File(YangTest.class.getResource(relativePath).toURI());
        FileReader fileReader = new FileReader(file);
        BufferedReader bufReader = new BufferedReader(fileReader);

        String line = null;
        StringBuilder result = new StringBuilder();
        while ((line = bufReader.readLine()) != null) {
            result.append(line);
        }
        bufReader.close();
        return result.toString();
    }

    private static void writeStringToFile(String text, String filePath) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(text.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document loadDocument(String xmlPath) throws IOException, org.xml.sax.SAXException {
        InputStream resourceAsStream = YangTest.class.getResourceAsStream(xmlPath);
        Document doc = UntrustedXML.newDocumentBuilder().parse(resourceAsStream);
        doc.getDocumentElement().normalize();
        return doc;
    }


    private static String toString(Node xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }

    public NormalizedNode jsonToNormalizedNodes(SchemaContext schemaContext) throws IOException, URISyntaxException, SAXException {

        //json转NormalizedNode
        System.out.println("<--------------->");
        NormalizedNodeResult result = new NormalizedNodeResult();
        NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        InstanceIdentifierContext<?> instanceIdentifierContext = this.buildInstanceIdentifierContext("openconfig-interfaces:interfaces", schemaContext);

        JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
                JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));

        System.out.println(JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));

        String inputJson = loadTextFile("/test.json");

        System.out.println(inputJson);

//        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
//        NormalizedNode<?, ?> transformedInput = result.getResult();

        InputStream entityStream = new ByteArrayInputStream(inputJson.getBytes(StandardCharsets.UTF_8));

        NormalizedNode<?, ?> transformedInput = readFrom(instanceIdentifierContext, entityStream, false);

        System.out.println("NormalizedNode:"+transformedInput);

        return transformedInput;
        //NormalizedNode转json
//        Writer writer = new StringWriter();
//        NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
//                JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext), SchemaPath.ROOT, null,
//                JsonWriterFactory.createJsonWriter(writer, 2));
//        NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
//        nodeWriter.write(transformedInput);
//        nodeWriter.close();
//
//        String jsonOutput = writer.toString();
//        writer.toString();
//        JsonParser parser = new JsonParser();
//        JsonElement serializedJson = parser.parse(jsonOutput);
//
//        System.out.println("序列化json"+serializedJson.toString());//序列化之后的json
//        System.out.println("原有json"+inputJson);//原有json

    }

    public NormalizedNode<?, ?> readFrom(
            final InstanceIdentifierContext<?> path, InputStream entityStream, final boolean isPost) {

        final NormalizedNodeResult resultHolder = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        final SchemaNode parentSchema;
        if (isPost) {
            parentSchema = path.getSchemaNode();
        } else if (path.getSchemaNode() instanceof SchemaContext) {
            parentSchema = path.getSchemaContext();
        } else {
            if (SchemaPath.ROOT.equals(path.getSchemaNode().getPath().getParent())) {
                parentSchema = path.getSchemaContext();
            } else {
                parentSchema = SchemaContextUtil
                        .findDataSchemaNode(path.getSchemaContext(), path.getSchemaNode().getPath().getParent());
            }
        }

        final JsonParserStream jsonParser = JsonParserStream.create(writer,
                JSONCodecFactorySupplier.RFC7951.getShared(path.getSchemaContext()), parentSchema);
        System.out.println("jsonParser:"+jsonParser);

        final JsonReader reader = new JsonReader(new InputStreamReader(entityStream));
        jsonParser.parse(reader);

        NormalizedNode<?, ?> result = resultHolder.getResult();
        System.out.println("result:"+result);

        return result;
    }

    public InstanceIdentifierContext<?> buildInstanceIdentifierContext(final String identifier,
                                                                       final SchemaContext schemaContext) {

        YangInstanceIdentifier deserialize = IdentifierCodec.deserialize(identifier, schemaContext);
        DataSchemaContextNode<?> child = DataSchemaContextTree.from(schemaContext).getChild(deserialize);
        if (child != null) {
            return new InstanceIdentifierContext(deserialize, child.getDataSchemaNode(), (DOMMountPoint) null, schemaContext);
        } else {
            QName rpcQName = deserialize.getLastPathArgument().getNodeType();
            RpcDefinition def = null;

            for(RpcDefinition rpcDefinition : schemaContext.findModule(rpcQName.getModule()).get().getRpcs()) {
                if (rpcDefinition.getQName().getLocalName().equals(rpcQName.getLocalName())) {
                    def = rpcDefinition;
                    break;
                }
            }

            return new InstanceIdentifierContext(deserialize, def, (DOMMountPoint) null, schemaContext);
        }

    }

    public void testXmlToNormalizedNode() throws URISyntaxException, IOException, ParserConfigurationException, SAXException, XMLStreamException, org.xml.sax.SAXException {

        /*
        由xml转NormalizedNode
         */
        QNameModule FOO_MODULE = QNameModule.create(URI.create("foo-namespace"));
        QName PARENT_CONTAINER = QName.create(FOO_MODULE, "parent-container");

        SchemaContext schemaContext = YangParserTestUtils.parseYangResourceDirectory("/foo/");
        ContainerSchemaNode parentContainerSchema = (ContainerSchemaNode) SchemaContextUtil.findNodeInSchemaContext(schemaContext,
                ImmutableList.of(PARENT_CONTAINER));

        System.out.println("加载yang:"+schemaContext);
        InputStream resourceAsStream = YangTest.class.getResourceAsStream("/foo/foo.xml");

        XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        NormalizedNodeResult result = new NormalizedNodeResult();
        NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, parentContainerSchema);
        xmlParser.parse(reader);

        NormalizedNode<?, ?> transformedInput = result.getResult();

        System.out.println("NormalizedNode:"+transformedInput);

        /*
        由NormalizedNode转xml
         */

//        SchemaContext schemaContext = YangParserTestUtils.parseYangResourceDirectory("/");
        Document doc = loadDocument("/foo/foo.xml");
        DOMResult domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(domResult);

        NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
                xmlStreamWriter, schemaContext);

        NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                xmlNormalizedNodeStreamWriter);

//        normalizedNodeWriter.write(buildOuterContainerNode());//此处可以通过一个函数构建
        normalizedNodeWriter.write(transformedInput);

        String expectedXml = toString(doc.getDocumentElement());//为了比较
        String serializedXml = toString(domResult.getNode());//序列化之后的
        System.out.println("原有xml："+expectedXml);//为了比较
        writeStringToFile(serializedXml.toString(), "newfoo.xml");
        System.out.println("序列化之后xml："+serializedXml);//序列化之后的


    }
}
