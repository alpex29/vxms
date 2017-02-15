package org.jacpfx.vertx.rest.response.blocking;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventBusStringCallBlocking;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Defines the fluent API to set the http error code in case of the onFailure method is executed
 */
public class ExecuteRSStringOnFailureCode extends ExecuteRSString {

    /**
     * The constructor to pass all needed members
     *
     * @param methodId                      the method identifier
     * @param vertx                         the vertx instance
     * @param failure                       the failure thrown while task execution
     * @param errorMethodHandler            the error handler
     * @param context                       the vertx routing context
     * @param headers                       the headers to pass to the response
     * @param stringSupplier                the supplier, producing the byte response
     * @param excecuteAsyncEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param encoder                       the encoder to encode your objects
     * @param errorHandler                  the error handler
     * @param onFailureRespond              the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode                the http status code to set for response
     * @param httpErrorCode                 the http error code to set in case of failure handling
     * @param retryCount                    the amount of retries before failure execution is triggered
     * @param timeout                       the amount of time before the execution will be aborted
     * @param delay                         the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout         the amount of time before the circuit breaker closed again
     */
    public ExecuteRSStringOnFailureCode(String methodId,
                                        Vertx vertx,
                                        Throwable failure,
                                        Consumer<Throwable> errorMethodHandler,
                                        RoutingContext context,
                                        Map<String, String> headers,
                                        ThrowableSupplier<String> stringSupplier,
                                        ExecuteEventBusStringCallBlocking excecuteAsyncEventBusAndReply,
                                        Encoder encoder,
                                        Consumer<Throwable> errorHandler,
                                        ThrowableFunction<Throwable, String> onFailureRespond,
                                        int httpStatusCode,
                                        int httpErrorCode,
                                        int retryCount,
                                        long timeout,
                                        long delay,
                                        long circuitBreakerTimeout) {
        super(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }


    /**
     * Define the HTTP Code in case of onFailure execution
     *
     * @param httpErrorCode the http error code to set for response, in case of error
     * @return the response chain {@link ExecuteRSString}
     */
    public ExecuteRSString httpErrorCode(HttpResponseStatus httpErrorCode) {
        return new ExecuteRSString(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                httpStatusCode,
                httpErrorCode.code(),
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }


}
