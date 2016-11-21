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
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class RESTJerseyClientStatefulCircuitBrakerAsyncTests extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    private static final String HOST = "127.0.0.1";
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
    public void stringGETResponseCircuitBaseTest() throws InterruptedException {
        Client client = ClientBuilder.newClient();

        //////// Request 1 -- creates crash
        WebTarget target = client.target("http://" + HOST + ":" + PORT).path("/wsService/stringGETResponseCircuitBaseTest/crash");
        Future<String> getCallback = target.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

            @Override
            public void completed(String response) {
                System.out.println("Response entity '" + response + "' received.");
                vertx.runOnContext(h -> {
                    System.out.println("--------");
                    assertEquals("failure", response);

                    //////// Request 1 -- is valid but crashes due to open circuit
                    WebTarget target2 = client.target("http://" + HOST + ":" + PORT).path("/wsService/stringGETResponseCircuitBaseTest/value");
                    target2.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

                        @Override
                        public void completed(String response) {
                            System.out.println("Response entity '" + response + "' received.");
                            vertx.runOnContext(h -> {
                                System.out.println("--------");
                                assertEquals("failure", response);


                                // wait 1s, but circuit is still open
                                vertx.setTimer(1205, handler -> {
                                    target2.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

                                        @Override
                                        public void completed(String response) {
                                            System.out.println("Response entity '" + response + "' received.");
                                            vertx.runOnContext(h -> {
                                                System.out.println("--------");
                                                assertEquals("failure", response);


                                                // wait another 1s, now circuit should be closed
                                                vertx.setTimer(1005, handler -> {
                                                    target2.request(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<String>() {

                                                        @Override
                                                        public void completed(String response) {
                                                            System.out.println("Response entity '" + response + "' received.");
                                                            vertx.runOnContext(h -> {
                                                                System.out.println("--------");
                                                                assertEquals("value", response);


                                                                testComplete();


                                                            });


                                                        }

                                                        @Override
                                                        public void failed(Throwable throwable) {

                                                        }
                                                    });
                                                });


                                            });


                                        }

                                        @Override
                                        public void failed(Throwable throwable) {

                                        }
                                    });
                                });


                            });


                        }

                        @Override
                        public void failed(Throwable throwable) {

                        }
                    });


                });
                // Assert.assertEquals(response, "test-123");

            }

            @Override
            public void failed(Throwable throwable) {

            }
        });

        await(80000, TimeUnit.MILLISECONDS);

    }


    public HttpClient getClient() {
        return client;
    }

    @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {

        /////------------- sync blocking ----------------

        @Path("/stringGETResponseCircuitBaseTest/:val")
        @GET
        public void stringGETResponseCircuitBaseTest(RestHandler reply) {
            final String val = reply.request().param("val");
            System.out.println("stringResponse: " + reply);
            reply.response().blocking().
                    stringResponse(() -> {
                        if (val.equals("crash")) throw new NullPointerException("test-123");
                        return val;
                    }).onError(e -> System.out.println(e.getMessage())).
                    retry(3).
                    closeCircuitBreaker(2000).
                    onFailureRespond(error->"failure").
                    execute();
        }


    }

}
