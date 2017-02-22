package org.jacpfx.vertx.event.eventbus.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.03.16.
 */
public class EventbusBridgeBlockingRequest {
    private final String methodId;
    private final Vertx vertx;
    private final Throwable t;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> requestmessage;

    public EventbusBridgeBlockingRequest(String methodId, Message<Object> requestmessage, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler) {
        this.methodId = methodId;
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.requestmessage = requestmessage;
    }


    public EventbusBridgeBlockingResponse send(String id, Object message) {
        return new EventbusBridgeBlockingResponse(methodId, requestmessage, vertx, t, errorMethodHandler, id, message, null);
    }
}
