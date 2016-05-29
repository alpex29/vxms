package org.jacpfx.registry;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.registry.DiscoveryClient;
import org.jacpfx.vertx.registry.EtcdRegistration;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Andy Moncsek on 23.04.15.
 */
public class BasicEtcRegTest extends VertxTestBase {
    private final static int MAX_RESPONSE_ELEMENTS = 4;
    public static final String SERVICE_REST_GET = "/wsService";
    public static final String SERVICE2_REST_GET = "/wsService2";
    private static final String HOST = "localhost";
    public static final int PORT = 9998;
    public static final int PORT2 = 9988;

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

        getVertx().deployVerticle(new BasicEtcRegTest.WsServiceOne(), options, asyncResult -> {
            // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
            System.out.println("start service: " + asyncResult.succeeded());
            assertTrue(asyncResult.succeeded());
            assertNotNull("deploymentID should not be null", asyncResult.result());
            // If deployed correctly then start the tests!
            //   latch2.countDown();

            latch2.countDown();

        });

        getVertx().deployVerticle(new BasicEtcRegTest.WsServiceTwo(), options, asyncResult -> {
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

    public void basicServiceRegistration() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                domainName("petShop").
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("instance1");
        reg.connect(result -> {
            if (result.succeeded()) {
                reg.retrieveKeys(root -> {
                    System.out.println(root.getNode());
                    testComplete();
                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        await();
    }

    @Test

    public void getKeys() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                domainName("petShop").
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("instance2");

        System.out.println("connect ");
        reg.connect(result -> {
            if (result.succeeded()) {
                reg.retrieveKeys(root -> {
                    org.jacpfx.vertx.registry.Node n1 = findNode(root.getNode(), "/petShop/myService");
                    System.out.println(n1);
                    assertEquals("/petShop/myService", n1.getKey());
                    testComplete();
                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        //  reg.disconnect(Future.factory.future());
        await();
    }

    @Test

    public void findService() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                domainName("petShop").
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("instance3");


        reg.connect(result -> {
            if (result.succeeded()) {
                final DiscoveryClient client = result.result();
                client.findService("/myService", service -> {
                    System.out.println("found: " + service.succeeded()+" node: "+service.getNode());
                    assertEquals("/petShop/myService", service.getNode().getKey());
                    testComplete();
                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        //  reg.disconnect(Future.factory.future());
        await();
    }

    @Test

    public void findNodes() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                domainName("petShop").
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("instance");




        reg.connect(result -> {
            if (result.succeeded()) {

                final DiscoveryClient client = result.result();
                client.findNode("/myService", node -> {
                    System.out.println(" found node : "+node.getNode());
                    testComplete();
                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        //  reg.disconnect(Future.factory.future());
        await();
    }

    @Test

    public void findServiceNode() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                domainName("petShop").
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("instance");




        reg.connect(result -> {
            if (result.succeeded()) {
                final DiscoveryClient client = result.result();

                client.findNode("/myService", node -> {
                    System.out.println(" found node : "+node.getServiceNode());
                    System.out.println(" found URI : "+node.getServiceNode().getUri().toString());
                    testComplete();
                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        //  reg.disconnect(Future.factory.future());
        await();
    }


    @Test

    public void connectServiceNode() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                domainName("testdomain").
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("instance");




        reg.connect(result -> {
            if (result.succeeded()) {

                final DiscoveryClient discoveryClient = result.result();
                discoveryClient.findNode(SERVICE_REST_GET, node -> {
                    assertTrue("did not find node",node.succeeded());
                    System.out.println(" found node : "+node.getServiceNode());
                    System.out.println(" found URI : "+node.getServiceNode().getUri().toString());
                    HttpClientOptions options = new HttpClientOptions();
                    HttpClient client = vertx.
                            createHttpClient(options);

                    HttpClientRequest request = client.getAbs(node.getServiceNode().getUri().toString()+"/endpointOne", resp -> {
                        resp.bodyHandler(body -> {
                            System.out.println("Got a response: " + body.toString());
                            Assert.assertEquals(body.toString(), "test");
                            testComplete();
                        });

                    });
                    request.end();

                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        //  reg.disconnect(Future.factory.future());
        await();
    }

    @Test

    public void connectServiceNodeTwo() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                domainName("testdomain").
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("instance");




        reg.connect(result -> {
            if (result.succeeded()) {

                final DiscoveryClient discoveryClient = result.result();
                discoveryClient.findNode(SERVICE_REST_GET, node -> {
                    assertTrue("did not find node",node.succeeded());
                    System.out.println(" found node : "+node.getServiceNode());
                    System.out.println(" found URI : "+node.getServiceNode().getUri().toString());
                    HttpClientOptions options = new HttpClientOptions();
                    HttpClient client = vertx.
                            createHttpClient(options);

                    HttpClientRequest request = client.getAbs(node.getServiceNode().getUri().toString()+"/endpointTwo/123", resp -> {
                        resp.bodyHandler(body -> {
                            System.out.println("Got.. a response: " + body.toString());
                            assertEquals(body.toString(), "123");
                            testComplete();
                        });

                    });
                    request.end();

                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        //  reg.disconnect(Future.factory.future());
        await();
    }

    @Test

    public void connectServiceNodeThree() throws InterruptedException {
        EtcdRegistration reg = EtcdRegistration.
                buildRegistration().
                vertx(vertx).
                etcdHost("127.0.0.1").
                etcdPort(4001).
                ttl(60).
                domainName("testdomain").
                serviceName("myService").
                serviceHost("localhost").
                servicePort(8080).
                nodeName("instance");




        reg.connect(result -> {
            if (result.succeeded()) {

                final DiscoveryClient discoveryClient = result.result();
                discoveryClient.findNode(SERVICE_REST_GET, node -> {
                    assertTrue("did not find node",node.succeeded());
                    System.out.println(" found node : "+node.getServiceNode());
                    System.out.println(" found URI : "+node.getServiceNode().getUri().toString());
                    HttpClientOptions options = new HttpClientOptions();
                    HttpClient client = vertx.
                            createHttpClient(options);

                    HttpClientRequest request = client.getAbs(node.getServiceNode().getUri().toString()+"/endpointThree/123", resp -> {
                        resp.bodyHandler(body -> {
                            System.out.println("Got.. a response: " + body.toString());
                            assertEquals(body.toString(), "WsServiceTwo:123");
                            testComplete();
                        });

                    });
                    request.end();

                });
            } else {
                assertTrue("connection failed", true);
                testComplete();
            }
        });


        //  reg.disconnect(Future.factory.future());
        await();
    }


    private org.jacpfx.vertx.registry.Node findNode(org.jacpfx.vertx.registry.Node node, String value) {
        System.out.println("find: " + node.getKey() + "  value:" + value);
        if (node.getKey() != null && node.getKey().equals(value)) return node;
        if (node.isDir() && node.getNodes()!=null) return node.getNodes().stream().filter(n1 -> {
            org.jacpfx.vertx.registry.Node n2 = n1.isDir() ? findNode(n1, value) : n1;
            return n2.getKey().equals(value);
        }).findFirst().orElse(new org.jacpfx.vertx.registry.Node(false, "", "", "", 0, 0, 0, Collections.emptyList()));
        return new org.jacpfx.vertx.registry.Node(false, "", "", "", 0, 0, 0, Collections.emptyList());
    }


    public HttpClient getClient() {
        return getVertx().
                createHttpClient(new HttpClientOptions());
    }

    @ServiceEndpoint(name = SERVICE_REST_GET, port = PORT)
    public class WsServiceOne extends VxmsEndpoint {
         DiscoveryClient client;
        public void postConstruct(final Future<Void> startFuture) {
            EtcdRegistration reg = EtcdRegistration.
                    buildRegistration().
                    vertx(vertx).
                    etcdHost("127.0.0.1").
                    etcdPort(4001).
                    ttl(60).
                    domainName("testdomain").
                    serviceName(SERVICE_REST_GET).
                    serviceHost(HOST).
                    servicePort(PORT).
                    nodeName(this.toString());
            reg.connect(result -> {
                client = result.result();
                startFuture.complete();
            });
        }

        @Path("/endpointOne")
        @GET
        public void rsEndpointOne(RestHandler reply) {
            System.out.println("wsEndpointOne: " + reply);
            reply.response().stringResponse(() -> "test").execute();
        }

        @Path("/endpointTwo/:help")
        @GET
        public void rsEndpointTwo(RestHandler handler) {
            System.out.println("wsEndpointTwo: " + handler);
            String productType = handler.request().param("help");
            System.out.println("wsEndpointTwo: " + handler);
            handler.response().stringResponse(() -> productType).execute();
        }

        @Path("/endpointThree/:help")
        @GET
        public void rsEndpointThree(RestHandler handler) {
            System.out.println("wsEndpointTwo: " + handler);

            client.findNode(SERVICE2_REST_GET, node -> {
                if(node.succeeded()) {
                    HttpClientOptions options = new HttpClientOptions();
                    HttpClient client = vertx.
                            createHttpClient(options);

                    HttpClientRequest request = client.getAbs(node.getServiceNode().getUri().toString()+"/endpointTwo/"+handler.request().param("help"), resp -> {
                        resp.bodyHandler(body -> {
                            System.out.println("Got a response: " + body.toString());
                            handler.response().stringResponse(() -> new String(body.getBytes())).execute();
                        });

                    });
                    request.end();
                }else {
                    String productType = handler.request().param("help");
                    System.out.println("wsEndpointTwo: " + handler);
                    handler.response().stringResponse(() -> productType).execute();
                }
            });
        }
    }
    @ServiceEndpoint(name = SERVICE2_REST_GET, port = PORT2)
    public class WsServiceTwo extends VxmsEndpoint {

        public void postConstruct(final Future<Void> startFuture) {
            EtcdRegistration reg = EtcdRegistration.
                    buildRegistration().
                    vertx(vertx).
                    etcdHost("127.0.0.1").
                    etcdPort(4001).
                    ttl(60).
                    domainName("testdomain").
                    serviceName(SERVICE2_REST_GET).
                    serviceHost(HOST).
                    servicePort(PORT2).
                    nodeName(this.toString());
            reg.connect(result -> {
                startFuture.complete();
            });
        }

        @Path("/endpointOne")
        @GET
        public void rsEndpointOne(RestHandler reply) {
            System.out.println("wsEndpointOne: " + reply);
            reply.response().stringResponse(() -> "test").execute();
        }

        @Path("/endpointTwo/:help")
        @GET
        public void rsEndpointTwo(RestHandler handler) {
            String productType = "WsServiceTwo:"+handler.request().param("help");
            System.out.println("wsEndpointTwo: " + handler);
            handler.response().stringResponse(() -> productType).execute();
        }
    }


}

