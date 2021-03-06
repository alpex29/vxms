/*
 * Copyright [2018] [Andy Moncsek]
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

package org.jacpfx.vxms.k8sfrontend.verticles;

import io.fabric8.annotations.ServiceName;
import io.fabric8.annotations.WithLabel;
import io.fabric8.annotations.WithLabels;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.jacpfx.vxms.common.ServiceEndpoint;
import org.jacpfx.vxms.k8s.annotation.K8SDiscovery;
import org.jacpfx.vxms.k8sfrontend.configuration.CustomRouterConfig;
import org.jacpfx.vxms.k8sfrontend.util.DefaultResponses;
import org.jacpfx.vxms.k8sfrontend.util.InitMongoDB;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.services.VxmsEndpoint;

/** Created by Andy Moncsek on 11.05.17. */
@ServiceEndpoint(port = 8181, name = "vxms-frontend", routerConf = CustomRouterConfig.class)
@K8SDiscovery
public class VxmsGateway extends VxmsEndpoint {

  @ServiceName()
  @WithLabels({
    @WithLabel(name = "name", value = "${read_name}"),
    @WithLabel(name = "version", value = "${read_version}")
  })
  private String read;

  @ServiceName()
  @WithLabels({
    @WithLabel(name = "name", value = "${write_name}"),
    @WithLabel(name = "version", value = "${write_version}")
  })
  private String write;

  Logger log = Logger.getLogger(VxmsGateway.class.getName());

  @Override
  public void postConstruct(final Future<Void> startFuture) {

    startFuture.complete();
  }



  @Path("/api/users")
  @GET
  public void userGet(RestHandler handler) {
    handler
        .response()
        .stringResponse(this::requestAllUsers)
        .timeout(2000)
        .retry(1)
        .closeCircuitBreaker(5000)
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond(
            (onError, future) ->
                future.complete(
                    new JsonArray()
                        .add(DefaultResponses.defaultErrorResponse(onError.getMessage()))
                        .encodePrettily()))
        .execute();
  }

  public void requestAllUsers(Future<String> future) {
    vertx
        .createHttpClient()
        .requestAbs(HttpMethod.GET, "http://" + read + "/read/api/users")
        .handler(resp -> writeResponse(future, resp))
        .exceptionHandler(th -> future.fail(th))
        .end();
  }

  @Path("/api/users/:id")
  @GET
  public void userGetById(RestHandler handler) {
    final String id = handler.request().param("id");
    if (id == null || id.isEmpty()) {
      handler.response().end(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    handler
        .response()
        .stringResponse(future -> requestUserById(id, future))
        .timeout(2000)
        .retry(1)
        .closeCircuitBreaker(5000)
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond(
            (onError, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(onError.getMessage()).encodePrettily()))
        .execute();
  }

  public void requestUserById(String id, Future<String> future) {
    vertx
        .createHttpClient()
        .requestAbs(HttpMethod.GET, "http://" + read + "/read/api/users/" + id)
        .handler(resp -> writeResponse(future, resp))
        .exceptionHandler(th -> future.fail(th))
        .end();
  }

  public void writeResponse(Future<String> future, HttpClientResponse resp) {
    resp.bodyHandler(body -> future.complete(body.getString(0, body.length())));
  }


  @Path("/api/users")
  @POST
  public void userPOST(RestHandler handler) {
    final Buffer body = handler.request().body();
    if (body == null || body.toJsonObject().isEmpty()) {
      handler.response().end(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    handler
        .response()
        .stringResponse(future -> insertUser(body, future))
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond(
            (onError, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(onError.getMessage()).encodePrettily()))
        .httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE)
        .execute();
  }

  public void insertUser(Buffer body, Future<String> future) {
    vertx
        .createHttpClient()
        .requestAbs(HttpMethod.POST, "http://" + write + "/write/api/users")
        .handler(resp -> writeResponse(future, resp))
        .exceptionHandler(th -> future.fail(th))
        .end(body);
  }

  @Path("/api/users/:id")
  @PUT
  public void userPutById(RestHandler handler) {
    final String id = handler.request().param("id");
    final Buffer body = handler.request().body();
    if (id == null || id.isEmpty() || body == null || body.toJsonObject().isEmpty()) {
      handler.response().end(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    final JsonObject user = DefaultResponses.mapToUser(body.toJsonObject(), id);
    handler
        .response()
        .stringResponse(future -> updateUser(user, future))
        .retry(2)
        .timeout(2000)
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond(
            (onError, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(onError.getMessage()).encodePrettily()))
        .httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE)
        .execute();
  }

  public void updateUser(JsonObject user, Future<String> future) {
    vertx
        .createHttpClient()
        .requestAbs(HttpMethod.PUT, "http://" + write + "/write/api/users")
        .handler(resp -> writeResponse(future, resp))
        .exceptionHandler(th -> future.fail(th))
        .end(Buffer.buffer(user.encode()));
  }

  @Path("/api/users/:id")
  @DELETE
  public void userDeleteById(RestHandler handler) {
    final String id = handler.request().param("id");
    if (id == null || id.isEmpty()) {
      handler.response().end(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    handler
        .response()
        .stringResponse(future -> deleteUser(id, future))
        .retry(2)
        .timeout(2000)
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond(
            (onError, future) ->
                future.complete(
                    DefaultResponses.defaultErrorResponse(onError.getMessage()).encodePrettily()))
        .httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE)
        .execute(HttpResponseStatus.NO_CONTENT);
  }

  public void deleteUser(String id, Future<String> future) {
    vertx
        .createHttpClient()
        .requestAbs(HttpMethod.DELETE, "http://" + write + "/write/api/users/" + id)
        .handler(resp -> writeResponse(future, resp))
        .exceptionHandler(th -> future.fail(th))
        .end();
  }

  @Path("/health")
  @GET
  public void health(RestHandler handler) {
    handler
        .response()
        .stringResponse(this::checkHealth)
        .onError(error -> log.log(Level.WARNING, "ERROR: " + error.getMessage()))
        .onFailureRespond((onError, future) -> future.complete(""))
        .httpErrorCode(HttpResponseStatus.SERVICE_UNAVAILABLE)
        .execute(HttpResponseStatus.OK);
  }

  private void checkHealth(Future<String> future) {
    future.complete("Ready");
  }

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    DeploymentOptions options =
        new DeploymentOptions()
            .setInstances(1)
            .setConfig(
                new JsonObject()
                    .put("kube.offline", true)
                    .put("local", true)
                    .put("read_name", "vxms-k8s-read")
                    .put("read_version", "1.2-SNAPSHOT")
                    .put("write_name", "vxms-k8s-write")
                    .put("write_version", "1.2-SNAPSHOT")
                    .put("name.vxms-k8s-read.version.1.2-SNAPSHOT", "localhost:7070")
                    .put("name.vxms-k8s-write.version.1.2-SNAPSHOT", "localhost:9090"));

    Vertx.vertx().deployVerticle(VxmsGateway.class.getName(), options);
  }
}
