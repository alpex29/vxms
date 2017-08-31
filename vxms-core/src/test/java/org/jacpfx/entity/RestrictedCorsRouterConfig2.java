package org.jacpfx.entity;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.jacpfx.vxms.common.configuration.RouterConfiguration;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public class RestrictedCorsRouterConfig2 implements RouterConfiguration {


  public void corsHandler(Router router) {
    router.route().handler(CorsHandler.create("http://example.com").
        allowedMethod(io.vertx.core.http.HttpMethod.GET).
        allowedMethod(io.vertx.core.http.HttpMethod.POST).
        allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS).
        allowedMethod(io.vertx.core.http.HttpMethod.PUT).
        allowedMethod(io.vertx.core.http.HttpMethod.DELETE).
        allowedHeader("Content-Type").
        allowedHeader("X-Requested-With"));
  }

  public BodyHandler bodyHandler() {
    return BodyHandler.create();
  }
}