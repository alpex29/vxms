package org.jacpfx;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.entity.RestrictedCorsEndpointConfig;
import org.jacpfx.entity.RestrictedCorsEndpointConfig2;
import org.jacpfx.vertx.rest.annotation.EndpointConfig;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientCORSTest extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "localhost";
    public static final int PORT = 9998;
    public static final int PORT2 = 9999;

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


        CountDownLatch latch2 = new CountDownLatch(2);
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

        getVertx().deployVerticle(new WsServiceTwo(), options, asyncResult -> {
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

    public void stringGETResponseSyncAsync() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + PORT).path("/wsService/stringGETResponseSyncAsync");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).header("Origin", "http://example.com").async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
            }

            @Override
            public void failed(Throwable throwable) {
                System.out.println(throwable.getMessage());
                Assert.assertEquals("javax.ws.rs.ForbiddenException: HTTP 403 CORS Rejected - Invalid origin",throwable.getMessage());
                latch.countDown();
            }
        });

        latch.await();
        testComplete();

    }

    @Test

    public void stringGETResponseAsyncSync() throws InterruptedException {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + PORT2).path("/wsService/stringGETResponseSyncAsync");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).header("Origin", "http://example.com").async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                Assert.assertEquals(response, "test-123");
                latch.countDown();
            }

            @Override
            public void failed(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        latch.await();
        testComplete();

    }




    public HttpClient getClient() {
        return client;
    }

    // TODO extend test for POST, OPTIONAL,....
    @ServiceEndpoint(value = SERVICE_REST_GET, port = PORT)
    @EndpointConfig(RestrictedCorsEndpointConfig.class)
    public class WsServiceOne extends VxmsEndpoint {

        /////------------- sync async ----------------

        @Path("/stringGETResponseSyncAsync")
        @GET
        public void rsstringGETResponseSyncAsync(RestHandler reply) {
            System.out.println("stringResponse: " + reply);
            reply.response().stringResponse(() -> {
                throw new NullPointerException("test-123");
            }).execute();
        }




    }

    @ServiceEndpoint(value = SERVICE_REST_GET, port = PORT2)
    @EndpointConfig(RestrictedCorsEndpointConfig2.class)
    public class WsServiceTwo extends VxmsEndpoint {

        /////------------- sync async ----------------

        @Path("/stringGETResponseSyncAsync")
        @GET
        public void rsstringGETResponseSyncAsync(RestHandler reply) {
            System.out.println("stringResponse: " + reply);
            reply.response().stringResponse(() -> {
                return "test-123";
            }).execute();
        }




    }

}
