package org.jacpfx.vertx.event.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusStringCallBlocking;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API for byte responses, defines access to failure handling, timeouts,...
 */
public class ExecuteEventbusStringResponse extends ExecuteEventbusString {

    /**
     * The constructor to pass all needed members
     *
     * @param methodId                      the method identifier
     * @param vertx                         the vertx instance
     * @param failure                       the failure thrown while task execution
     * @param errorMethodHandler            the error handler
     * @param message                       the message to responde to
     * @param stringSupplier                the supplier, producing the byte response
     * @param excecuteAsyncEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param errorHandler                  the error handler
     * @param onFailureRespond              the consumer that takes a Future with the alternate response value in case of failure
     * @param deliveryOptions               the response delivery options
     * @param retryCount                    the amount of retries before failure execution is triggered
     * @param timeout                       the amount of time before the execution will be aborted
     * @param delay                         the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout         the amount of time before the circuit breaker closed again
     */
    public ExecuteEventbusStringResponse(String methodId,
                                         Vertx vertx,
                                         Throwable failure,
                                         Consumer<Throwable> errorMethodHandler,
                                         Message<Object> message,
                                         ThrowableSupplier<String> stringSupplier,
                                         ExecuteEventbusStringCallBlocking excecuteAsyncEventBusAndReply,
                                         Consumer<Throwable> errorHandler,
                                         ThrowableFunction<Throwable, String> onFailureRespond,
                                         DeliveryOptions deliveryOptions,
                                         int retryCount,
                                         long timeout,
                                         long delay,
                                         long circuitBreakerTimeout) {
        super(methodId, vertx, failure,
                errorMethodHandler,
                message,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                delay, circuitBreakerTimeout);
    }


    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the createResponse chain {@link ExecuteEventbusString}
     */
    public ExecuteEventbusString onFailureRespond(ThrowableFunction<Throwable, String> onFailureRespond) {
        return new ExecuteEventbusString(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

    /**
     * intermediate error handler which will be called on each error (at least 1 time, in case on N retries... up to N times)
     *
     * @param errorHandler the handler to be executed on each error
     * @return the response chain {@link ExecuteEventbusStringResponse}
     */
    public ExecuteEventbusStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteEventbusStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

    /**
     * retry operation on error
     *
     * @param retryCount the amount of retries before failing the operation
     * @return the createResponse chain {@link ExecuteEventbusStringCircuitBreaker}
     */
    public ExecuteEventbusStringCircuitBreaker retry(int retryCount) {
        return new ExecuteEventbusStringCircuitBreaker(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringSupplier,
                excecuteAsyncEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout time to wait in ms
     * @return the createResponse chain
     */
    public ExecuteEventbusStringResponse timeout(long timeout) {
        return new ExecuteEventbusStringResponse(methodId, vertx, failure, errorMethodHandler, message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }

    /**
     * Defines the delay (in ms) between the createResponse retries (on error).
     *
     * @param delay
     * @return the createResponse chain
     */
    public ExecuteEventbusStringResponse delay(long delay) {
        return new ExecuteEventbusStringResponse(methodId, vertx, failure, errorMethodHandler, message, stringSupplier, excecuteAsyncEventBusAndReply, errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, delay, circuitBreakerTimeout);
    }


}
