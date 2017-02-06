package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.basic.ExecuteEventBusStringCall;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicStringResponse extends ExecuteRSBasicString {


    public ExecuteRSBasicStringResponse(String methodId,
                                        Vertx vertx,
                                        Throwable t,
                                        Consumer<Throwable> errorMethodHandler,
                                        RoutingContext context,
                                        Map<String, String> headers,
                                        ThrowableFutureConsumer<String> stringConsumer,
                                        ExecuteEventBusStringCall excecuteEventBusAndReply,
                                        Encoder encoder,
                                        Consumer<Throwable> errorHandler,
                                        ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                        int httpStatusCode, int httpErrorCode,
                                        int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }


    /**
     * intermediate error handler which will be called on each error (at least 1 time, in case on N retries... up to N times)
     *
     * @param errorHandler the handler to be executed on each error
     * @return the response chain
     */
    public ExecuteRSBasicStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout the amount of timeout in ms
     * @return the response chain
     */
    public ExecuteRSBasicStringResponse timeout(long timeout) {
        return new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * retry execution N times before
     *
     * @param retryCount the amount of retries
     * @return the response chain
     */
    public ExecuteRSBasicStringCircuitBreaker retry(int retryCount) {
        return new ExecuteRSBasicStringCircuitBreaker(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value, this handler is a terminal handler and will be executed only once
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the response chain
     */
    public ExecuteRSBasicStringOnFailureCode onFailureRespond(ThrowableErrorConsumer<Throwable, String> onFailureRespond) {
        return new ExecuteRSBasicStringOnFailureCode(methodId, vertx, t, errorMethodHandler, context, headers, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }

    /**
     * put HTTP header to response
     *
     * @param key   the header name
     * @param value the header value
     * @return the response chain
     */
    public ExecuteRSBasicStringResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicStringResponse(methodId, vertx, t, errorMethodHandler, context, headerMap, stringConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }
}
