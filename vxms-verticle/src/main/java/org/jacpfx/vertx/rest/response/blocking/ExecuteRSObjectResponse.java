package org.jacpfx.vertx.rest.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusObjectCallAsync;
import org.jacpfx.common.encoder.Encoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSObjectResponse extends ExecuteRSObject {


    public ExecuteRSObjectResponse(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<Serializable> objectSupplier, ExecuteEventBusObjectCallAsync excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> onFailureRespond, int httpStatusCode, int retryCount, long timeout, long delay) {
        super(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSObject onFailureRespond(Function<Throwable, Serializable> onFailureRespond, Encoder encoder) {
        return new ExecuteRSObject(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * Will be executed on each error
     * @param errorHandler
     * @return the createResponse chain
     */
    public ExecuteRSObjectResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    public ExecuteRSObjectResponse retry(int retryCount) {
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain
     */
    public ExecuteRSObjectResponse timeout(long timeout) {
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries (on error).
     *
     * @param delay
     * @return the createResponse chain
     */
    public ExecuteRSObjectResponse delay(long delay) {
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headers, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }


    public ExecuteRSObjectResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSObjectResponse(vertx, t, errorMethodHandler, context, headerMap, objectSupplier, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, delay);
    }
}
