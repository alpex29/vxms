package org.jacpfx.vertx.rest.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.blocking.ExecuteEventBusObjectCallBlocking;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * This class defines the fluid API part to define the amount of time after the circuit breaker will be closed again
 */
public class ExecuteRSObjectCircuitBreaker extends ExecuteRSObjectResponse {

    /**
     * The constructor to pass all needed members
     *
     * @param methodId                         the method identifier
     * @param vertx                            the vertx instance
     * @param failure                          the failure thrown while task execution
     * @param errorMethodHandler               the error handler
     * @param context                          the vertx routing context
     * @param headers                          the headers to pass to the response
     * @param objectSupplier                   the supplier, producing the object response
     * @param excecuteBlockingEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param encoder                          the encoder to encode your objects
     * @param errorHandler                     the error handler
     * @param onFailureRespond                 the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode                   the http status code to set for response
     * @param httpErrorCode                    the http error code to set in case of failure handling
     * @param retryCount                       the amount of retries before failure execution is triggered
     * @param timeout                          the amount of time before the execution will be aborted
     * @param delay                            the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout            the amount of time before the circuit breaker closed again
     */
    public ExecuteRSObjectCircuitBreaker(String methodId,
                                         Vertx vertx,
                                         Throwable failure,
                                         Consumer<Throwable> errorMethodHandler,
                                         RoutingContext context,
                                         Map<String, String> headers,
                                         ThrowableSupplier<Serializable> objectSupplier,
                                         ExecuteEventBusObjectCallBlocking excecuteBlockingEventBusAndReply,
                                         Encoder encoder,
                                         Consumer<Throwable> errorHandler,
                                         ThrowableFunction<Throwable, Serializable> onFailureRespond,
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
                objectSupplier,
                excecuteBlockingEventBusAndReply,
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
     * Defines how long a method can be executed before aborted.
     *
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain {@link ExecuteRSObjectResponse}
     */
    public ExecuteRSObjectResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteRSObjectResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                context,
                headers,
                objectSupplier,
                excecuteEventBusAndReply,
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


}
