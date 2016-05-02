package org.jacpfx.vertx.rest.configuration;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;

/**
 * Created by Andy Moncsek on 18.02.16.
 */
public interface EndpointConfiguration {

    default void corsHandler(Router router) {

    }

    default void bodyHandler(Router router) {
        router.route().handler(BodyHandler.create());
    }

    default void cookieHandler(Router router) {
        router.route().handler(CookieHandler.create());
    }

    default void staticHandler(Router router) {

    }

    default void sessionHandler(Vertx vertx, Router router) {

    }

    default void customRouteConfiguration(Vertx vertx, Router router) {

    }
}
