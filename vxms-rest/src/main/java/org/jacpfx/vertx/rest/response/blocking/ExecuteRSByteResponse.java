package org.jacpfx.vertx.rest.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCallAsync;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSByteResponse extends ExecuteRSByte {


    public ExecuteRSByteResponse(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context,
                                 Map<String, String> headers, ThrowableSupplier<byte[]> byteSupplier, ExecuteEventBusByteCallAsync excecuteAsyncEventBusAndReply,
                                 Encoder encoder, Consumer<Throwable> errorHandler, ThrowableFunction<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long delay, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain
     */
    public ExecuteRSByte onFailureRespond(ThrowableFunction<Throwable, byte[]> onFailureRespond) {
        return new ExecuteRSByte(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler,
                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Will be executed on each error
     *
     * @param errorHandler
     * @return the createResponse chain
     */
    public ExecuteRSByteResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler,
                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * retry operation on error
     *
     * @param retryCount
     * @return the createResponse chain
     */
    public ExecuteRSByteCircuitBreaker retry(int retryCount) {
        return new ExecuteRSByteCircuitBreaker(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler,
                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain
     */
    public ExecuteRSByteResponse timeout(long timeout) {
        return new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler,
                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries.
     *
     * @param delay
     * @return the createResponse chain
     */
    public ExecuteRSByteResponse delay(long delay) {
        return new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler,
                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * put HTTP header to response
     *
     * @param key   the header name
     * @param value the header value
     * @return the response chain
     */
    public ExecuteRSByteResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSByteResponse(methodId, vertx, t, errorMethodHandler, context, headerMap, byteSupplier, excecuteAsyncEventBusAndReply, encoder, errorHandler,
                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, delay, circuitBreakerTimeout);
    }
}
