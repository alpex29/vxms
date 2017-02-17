package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.vertx.rest.eventbus.basic.EventBusRequest;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 07.01.16.
 * The RestHandler gives access to the {@link RoutingContext} , the {@link RSRequest} , the {@link RSResponse} and the {@link EventBusRequest}. It is the Entry point to the fluent API to perform tasks and create responses.
 */
public class RestHandler {
    private final Vertx vertx;
    private final Throwable failure;
    private final Consumer<Throwable> errorMethodHandler;
    private final RoutingContext context;
    private final String methodId;

    /**
     * The constructor initialize the Rest handler
     *
     * @param methodId           the method identifier
     * @param context            the vertx routing context
     * @param vertx              the vertx instance
     * @param failure            the failure thrown while task execution or messaging
     * @param errorMethodHandler the error-method handler
     */
    public RestHandler(String methodId, RoutingContext context, Vertx vertx, Throwable failure, Consumer<Throwable> errorMethodHandler) {
        this.methodId = methodId;
        this.context = context;
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
    }


    /**
     * Returns the Vert.x http Routing context
     *
     * @return {@link RoutingContext}
     */
    public RoutingContext context() {
        return this.context;
    }

    /**
     * Returns the data wrapper to access the http request, attributes and parameters.
     *
     * @return {@link RSRequest}
     */
    public RSRequest request() {
        return new RSRequest(context);
    }

    /**
     * Starts the fluent API handling to execute tasks and create a response
     *
     * @return {@link RSResponse}
     */
    public RSResponse response() {
        return new RSResponse(methodId, vertx, failure, errorMethodHandler, context, new HashMap<>());
    }

    /**
     * Starts the fluent API to create an Event bus request, to perform a task and to create a response
     *
     * @return {@link EventBusRequest}
     */
    public EventBusRequest eventBusRequest() {
        return new EventBusRequest(methodId, vertx, failure, errorMethodHandler, context);
    }

}
