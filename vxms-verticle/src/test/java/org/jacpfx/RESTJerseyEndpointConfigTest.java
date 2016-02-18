package org.jacpfx;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.FileUpload;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.entity.MyCustomEndpointConfig;
import org.jacpfx.vertx.rest.annotation.EndpointConfig;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyEndpointConfigTest extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "localhost";
    public static final int PORT = 9998;

    protected int getNumNodes() {
        return 1;
    }

    protected Vertx getVertx() {
        return vertices[0];
    }

    @Override
    protected ClusterManager getClusterManager() {
        return new FakeClusterManager();
    }


    private HttpClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startNodes(getNumNodes());

    }

    @Before
    public void startVerticles() throws InterruptedException {


        CountDownLatch latch2 = new CountDownLatch(1);
        DeploymentOptions options = new DeploymentOptions().setInstances(1);
        options.setConfig(new JsonObject().put("clustered", false).put("host", HOST));
        // Deploy the module - the System property `vertx.modulename` will contain the name of the module so you
        // don't have to hardecode it in your tests

        getVertx().deployVerticle(new WsServiceOne(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service: " + asyncResult.succeeded());
            assertTrue(asyncResult.succeeded());
            assertNotNull("deploymentID should not be null", asyncResult.result());
            // If deployed correctly then start the tests!
            //   latch2.countDown();

            latch2.countDown();

        });

        client = getVertx().
                createHttpClient(new HttpClientOptions());
        awaitLatch(latch2);

    }




    @Test
    public void stringPOSTResponseWithParameter() throws InterruptedException, ExecutionException, IOException {
        final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
        File file = new File(getClass().getClassLoader().getResource("payload.xml").getFile());
        final FileDataBodyPart filePart = new FileDataBodyPart("file", file);
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("foo", "bar").field("hello","world").bodyPart(filePart);

        WebTarget target = client.target("http://localhost:"+PORT).path("/wsService/simpleFilePOSTupload");
        final Response response = target.request().post(Entity.entity(multipart, multipart.getMediaType()));

        //Use response object to verify upload success
        final String entity = response.readEntity(String.class);
        System.out.println(entity);
        assertTrue(entity.equals("barworlddfgdfg"));
        formDataMultiPart.close();
        multipart.close();
        testComplete();

    }


    public HttpClient getClient() {
        return client;
    }


    @ServiceEndpoint(value = SERVICE_REST_GET, port = PORT)
    @EndpointConfig(MyCustomEndpointConfig.class)
    public class WsServiceOne extends VxmsEndpoint {



        @Path("/simpleFilePOSTupload")
        @POST
        public void rsstringPOSTResponse(RestHandler handler) {
            handler.response().async().stringResponse(()-> {
                Set<FileUpload> files = handler.request().fileUploads();
                System.out.println("FILES: "+files+"   "+handler.request().param("foo"));
                FileUpload f = files.iterator().next();
                System.out.println("name: "+f.fileName()+"  ");
                Buffer uploaded = vertx.fileSystem().readFileBlocking(f.uploadedFileName());
                Document payload = obtenerDocumentDeByte(uploaded.getBytes());
                String payloadValue = payload.getElementsByTagName("test").item(0).getTextContent();
                System.out.println("payload:  "+payload.getElementsByTagName("test").item(0).getTextContent());
                return handler.request().param("foo")+handler.request().param("hello")+payloadValue;
            }).execute();
        }

        private Document obtenerDocumentDeByte(byte[] documentoXml) throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(documentoXml));
        }

    }



}
