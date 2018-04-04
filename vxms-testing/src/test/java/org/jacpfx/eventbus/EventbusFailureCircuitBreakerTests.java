/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.eventbus;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import java.util.concurrent.CountDownLatch;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.event.annotation.Consume;
import org.jacpfx.vxms.event.response.EventbusHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;
import org.junit.Before;
import org.junit.Test;

/** Created by Andy Moncsek on 23.04.15. */
public class EventbusFailureCircuitBreakerTests extends VertxTestBase {

  public static final String SERVICE_REST_GET = "/wsService";
  public static final int PORT = 0;
  private static final int MAX_RESPONSE_ELEMENTS = 4;
  private static final String HOST = "127.0.0.1";
  private HttpClient client;

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
    // Deploy the module - the System property `vertx.modulename` will contain the name of the
    // module so you
    // don'failure have to hardecode it in your tests
    WsServiceOne one = new WsServiceOne();
    one.init(vertx, vertx.getOrCreateContext());
    getVertx()
        .deployVerticle(
            one,
            options,
            asyncResult -> {
              // Deployment is asynchronous and this this handler will be called when it's complete
              // (or failed)
              System.out.println("start service: " + asyncResult.succeeded());
              assertTrue(asyncResult.succeeded());
              assertNotNull("deploymentID should not be null", asyncResult.result());
              // If deployed correctly then start the tests!
              //   latch2.countDown();

              latch2.countDown();
            });

    client = getVertx().createHttpClient(new HttpClientOptions());
    awaitLatch(latch2);
  }

  @Test
  public void simpleStringResponseFailure() throws InterruptedException {
    getVertx()
        .eventBus()
        .send(
            SERVICE_REST_GET + "/simpleStringResponseFailure",
            "crash",
            res -> {
              assertTrue(res.succeeded());
              assertEquals("failure", res.result().body().toString());
              System.out.println("out: " + res.result().body().toString());
              getVertx()
                  .eventBus()
                  .send(
                      SERVICE_REST_GET + "/simpleStringResponseFailure",
                      "val",
                      res2 -> {
                        assertTrue(res2.succeeded());
                        assertEquals("failure", res2.result().body().toString());
                        System.out.println("out: " + res2.result().body().toString());

                        // wait 1s, but circuit is still open
                        vertx.setTimer(
                            1205,
                            handler -> {
                              getVertx()
                                  .eventBus()
                                  .send(
                                      SERVICE_REST_GET + "/simpleStringResponseFailure",
                                      "val",
                                      res3 -> {
                                        assertTrue(res3.succeeded());
                                        assertEquals("failure", res3.result().body().toString());
                                        System.out.println(
                                            "out: " + res3.result().body().toString());

                                        // wait another 1s, now circuit should be closed
                                        vertx.setTimer(
                                            1005,
                                            handler2 -> {
                                              getVertx()
                                                  .eventBus()
                                                  .send(
                                                      SERVICE_REST_GET
                                                          + "/simpleStringResponseFailure",
                                                      "val",
                                                      res4 -> {
                                                        assertTrue(res4.succeeded());
                                                        assertEquals(
                                                            "val", res4.result().body().toString());
                                                        System.out.println(
                                                            "out: "
                                                                + res4.result().body().toString());

                                                        testComplete();
                                                      });
                                            });
                                      });
                            });
                      });
            });
    await();
  }

  public HttpClient getClient() {
    return client;
  }

  @ServiceEndpoint(name = SERVICE_REST_GET, contextRoot = SERVICE_REST_GET, port = PORT)
  public class WsServiceOne extends VxmsEndpoint {

    @Consume("/simpleStringResponseFailure")
    public void simpleStringResponseFailure(EventbusHandler reply) {
      System.out.println("simpleStringResponseFailure: " + reply);
      String value = reply.request().body().toString();
      reply
          .response()
          .stringResponse(
              (future) -> {
                if (value.equals("crash")) {
                  throw new NullPointerException("test-123");
                }
                future.complete(value);
              })
          .onError(e -> System.out.println(e.getMessage()))
          .retry(3)
          .closeCircuitBreaker(2000)
          .onFailureRespond((error, future) -> future.complete("failure"))
          .execute();
    }
  }
}
