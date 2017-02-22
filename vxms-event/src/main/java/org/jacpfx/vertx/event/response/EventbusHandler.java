package org.jacpfx.vertx.event.response;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jacpfx.vertx.event.eventbus.basic.EventbusBridgeRequest;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 07.01.16.
 * The EventbusHandler gives access to the {@link Message} , the {@link EventbusRequest} , the {@link EventbusResponse} and the {@link EventbusBridgeRequest}.
 */
public class EventbusHandler {
    private final Vertx vertx;
    private final Throwable failure;
    private final Consumer<Throwable> errorMethodHandler;
    private final Message<Object> message;
    private final String methodId;

    /**
     * The constructor initialize the Eventbus Handler
     *
     * @param methodId           the method identifier
     * @param message            the message to respond to
     * @param vertx              the vertx instance
     * @param failure            the failure thrown while task execution or messaging
     * @param errorMethodHandler the error-method handler
     */
    public EventbusHandler(String methodId, Message<Object> message, Vertx vertx, Throwable failure, Consumer<Throwable> errorMethodHandler) {
        this.methodId = methodId;
        this.message = message;
        this.vertx = vertx;
        this.failure = failure;
        this.errorMethodHandler = errorMethodHandler;
    }


    /**
     * Returns the message to respond to
     *
     * @return {@link Message}
     */
    public Message<Object> message() {
        return this.message;
    }

    /**
     * Returns the wrapped message to get access to message body
     *
     * @return {@link EventbusRequest}
     */
    public EventbusRequest request() {
        return new EventbusRequest(message);
    }

    /**
     * Starts the response chain to respond to message
     *
     * @return {@link EventbusResponse}
     */
    public EventbusResponse response() {
        return new EventbusResponse(methodId, message, vertx, failure, errorMethodHandler);
    }

    /**
     * Starts the event-bus bridge chain to send a message and to use the response of this message to create the main response
     *
     * @return {@link EventbusBridgeRequest}
     */
    public EventbusBridgeRequest eventBusRequest() {
        return new EventbusBridgeRequest(methodId, message, vertx, failure, errorMethodHandler);
    }

}
