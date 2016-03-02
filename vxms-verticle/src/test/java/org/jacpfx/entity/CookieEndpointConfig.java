package org.jacpfx.entity;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import org.jacpfx.vertx.rest.configuration.EndpointConfiguration;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public class CookieEndpointConfig implements EndpointConfiguration {

    public void cookieHandler(Router router) {
        router.route().handler(CorsHandler.create("localhost").
                allowedMethod(io.vertx.core.http.HttpMethod.GET).
                allowedMethod(io.vertx.core.http.HttpMethod.POST).
                allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS).
                allowedMethod(io.vertx.core.http.HttpMethod.PUT).
                allowedMethod(io.vertx.core.http.HttpMethod.DELETE).
                allowedHeader("Content-Type").
                allowedHeader("X-Requested-With"));
    }


}