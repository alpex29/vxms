package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.basic.ExecuteEventbusStringCall;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API for byte responses, defines access to failure handling, timeouts,...
 */
public class ExecuteRSBasicStringResponse extends ExecuteRSBasicString {


    /**
     * The constructor to pass all needed members
     *
     * @param methodId                 the method identifier
     * @param vertx                    the vertx instance
     * @param failure                  the failure thrown while task execution
     * @param errorMethodHandler       the error handler
     * @param context                  the vertx routing context
     * @param headers                  the headers to pass to the response
     * @param stringConsumer           the consumer that takes a Future to complete, producing the string response
     * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param encoder                  the encoder to encode your objects
     * @param errorHandler             the error handler
     * @param onFailureRespond         the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode           the http status code to set for response
     * @param httpErrorCode            the http error code to set in case of failure handling
     * @param retryCount               the amount of retries before failure execution is triggered
     * @param timeout                  the amount of time before the execution will be aborted
     * @param circuitBreakerTimeout    the amount of time before the circuit breaker closed again
     */
    public ExecuteRSBasicStringResponse(String methodId,
                                        Vertx vertx,
                                        Throwable failure,
                                        Consumer<Throwable> errorMethodHandler,
                                        RoutingContext context,
                                        Map<String, String> headers,
                                        ThrowableFutureConsumer<String> stringConsumer,
                                        ExecuteEventbusStringCall excecuteEventBusAndReply,
                                        Encoder encoder,
                                        Consumer<Throwable> errorHandler,
                                        ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                        int httpStatusCode, int httpErrorCode,
                                        int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringConsumer,
                excecuteEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }


    /**
     * intermediate error handler which will be called on each error (at least 1 time, in case on N retries... up to N times)
     *
     * @param errorHandler the handler to be executed on each error
     * @return the response chain {@link ExecuteRSBasicStringResponse}
     */
    public ExecuteRSBasicStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteRSBasicStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringConsumer,
                excecuteEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout the amount of timeout in ms
     * @return the response chain {@link ExecuteRSBasicStringResponse}
     */
    public ExecuteRSBasicStringResponse timeout(long timeout) {
        return new ExecuteRSBasicStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringConsumer,
                excecuteEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }

    /**
     * retry execution N times before
     *
     * @param retryCount the amount of retries
     * @return the response chain {@link ExecuteRSBasicStringCircuitBreaker}
     */
    public ExecuteRSBasicStringCircuitBreaker retry(int retryCount) {
        return new ExecuteRSBasicStringCircuitBreaker(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringConsumer,
                excecuteEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value, this handler is a terminal handler and will be executed only once
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the response chain {@link ExecuteRSBasicStringOnFailureCode}
     */
    public ExecuteRSBasicStringOnFailureCode onFailureRespond(ThrowableErrorConsumer<Throwable, String> onFailureRespond) {
        return new ExecuteRSBasicStringOnFailureCode(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringConsumer,
                excecuteEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }

    /**
     * put HTTP header to response
     *
     * @param key   the header name
     * @param value the header value
     * @return the response chain {@link ExecuteRSBasicStringResponse}
     */
    public ExecuteRSBasicStringResponse putHeader(String key, String value) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put(key, value);
        return new ExecuteRSBasicStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headerMap,
                stringConsumer,
                excecuteEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }
}
