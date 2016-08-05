package org.jacpfx.vertx.websocket.response;

import io.vertx.core.Vertx;
import org.jacpfx.vertx.websocket.registry.WebSocketEndpoint;
import org.jacpfx.vertx.websocket.registry.WebSocketRegistry;
import org.jacpfx.vertx.websocket.util.CommType;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 17.12.15.
 * The TargetType defines the target of the response.
 */
public class WSResponse {
    protected final WebSocketEndpoint endpoint;
    protected final Vertx vertx;
    protected final WebSocketRegistry registry;
    protected final Consumer<Throwable> errorMethodHandler;

    protected WSResponse(WebSocketEndpoint endpoint, Vertx vertx, WebSocketRegistry registry, Consumer<Throwable> errorMethodHandler) {
        this.endpoint = endpoint;
        this.vertx = vertx;
        this.registry = registry;
        this.errorMethodHandler = errorMethodHandler;
    }

    /**
     * Returns the blocking response handler
     *
     * @return @see{org.jacpfx.vertx.websocket.response.TargetTypeAsync}
     */
    public WSResponseBlocking blocking() {
        return new WSResponseBlocking(endpoint, vertx, registry, errorMethodHandler);
    }

    /**
     * The response will be returned to all connected sessions
     *
     * @return @see{org.jacpfx.vertx.websocket.response.ResponseType}
     */
    public ResponseType toAll() {
        return new ResponseType(new WebSocketEndpoint[]{endpoint}, vertx, CommType.ALL, errorMethodHandler, registry);
    }


    /**
     * The response will be returned to all connected sessions, except the passed endpoints
     *
     * @param endpoints the endpoints to exclude
     * @return @see{org.jacpfx.vertx.websocket.response.ResponseType}
     */
    public ResponseType toAllBut(WebSocketEndpoint... endpoints) {
        // TODO iteration over stream / filter
        return new ResponseType(endpoints, vertx, CommType.ALL_BUT_CALLER, errorMethodHandler, registry);
    }

    /**
     * The response will be returned to the caller
     *
     * @return @see{org.jacpfx.vertx.websocket.response.ResponseType}
     */
    public ResponseType reply() {
        return new ResponseType(new WebSocketEndpoint[]{endpoint}, vertx, CommType.CALLER, errorMethodHandler, registry);
    }

    /**
     * The response will be returned to all passed endpoints
     *
     * @param endpoints the endpoints to reply to
     * @return @see{org.jacpfx.vertx.websocket.response.ResponseType}
     */
    public ResponseType to(WebSocketEndpoint... endpoints) {
        return new ResponseType(endpoints, vertx, CommType.TO, errorMethodHandler, registry);
    }


}
