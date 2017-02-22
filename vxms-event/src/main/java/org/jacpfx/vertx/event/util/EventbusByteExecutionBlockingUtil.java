package org.jacpfx.vertx.event.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.event.eventbus.blocking.EventbusBridgeBlockingExecution;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusByteCallBlocking;
import org.jacpfx.vertx.event.interfaces.blocking.RecursiveBlockingExecutor;
import org.jacpfx.vertx.event.interfaces.blocking.RetryBlockingExecutor;
import org.jacpfx.vertx.event.response.blocking.ExecuteEventbusByteResponse;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Typed execution of event-bus calls and blocking object response
 */
public class EventbusByteExecutionBlockingUtil {
    /**
     * create execution chain for event-bus request and reply to the event
     *
     * @param _methodId               the method identifier
     * @param _targetId               the event-bus target id
     * @param _message                the message to send
     * @param _byteFunction           the function to process the result message
     * @param _requestDeliveryOptions the event-bus delivery options
     * @param _vertx                  the vertx instance
     * @param _failure                the failure thrown while task execution
     * @param _errorMethodHandler     the error-method handler
     * @param _requestMessage         the event-bus request options
     * @param _byteSupplier           the supplier, producing the byte response
     * @param _errorHandler           the error handler
     * @param _onFailureRespond       the consumer that takes a Future with the alternate response value in case of failure
     * @param _retryCount             the amount of retries before failure execution is triggered
     * @param _timeout                the amount of time before the execution will be aborted
     * @param _delay                  the delay between an error and the retry
     * @param _circuitBreakerTimeout  the amount of time before the circuit breaker closed again
     * @return the execution chain {@link ExecuteEventbusByteResponse}
     */
    public static ExecuteEventbusByteResponse mapToByteResponse(String _methodId,
                                                                String _targetId,
                                                                Object _message,
                                                                ThrowableFunction<AsyncResult<Message<Object>>, byte[]> _byteFunction,
                                                                DeliveryOptions _requestDeliveryOptions,
                                                                Vertx _vertx,
                                                                Throwable _failure,
                                                                Consumer<Throwable> _errorMethodHandler,
                                                                Message<Object> _requestMessage,
                                                                ThrowableSupplier<byte[]> _byteSupplier,
                                                                Consumer<Throwable> _errorHandler,
                                                                ThrowableFunction<Throwable, byte[]> _onFailureRespond,
                                                                DeliveryOptions _responseDeliveryOptions,
                                                                int _retryCount,
                                                                long _timeout,
                                                                long _delay,
                                                                long _circuitBreakerTimeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_requestDeliveryOptions).orElse(new DeliveryOptions());

        final RetryBlockingExecutor retry = (targetId,
                                             message,
                                             function,
                                             requestDeliveryOptions,
                                             methodId,
                                             vertx, t,
                                             errorMethodHandler,
                                             requestMessage,
                                             supplier,
                                             encoder,
                                             errorHandler,
                                             onFailureRespond,
                                             responseDeliveryOptions,
                                             retryCount,
                                             timeout, delay, circuitBreakerTimeout) -> {
            int retryValue = retryCount - 1;
            mapToByteResponse(methodId, targetId,
                    message,
                    function,
                    requestDeliveryOptions,
                    vertx, t, errorMethodHandler,
                    requestMessage,
                    null,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryValue,
                    timeout,
                    delay,
                    circuitBreakerTimeout).
                    execute();

        };

        final RecursiveBlockingExecutor executor = (methodId,
                                                    vertx, t,
                                                    errorMethodHandler,
                                                    requestMessage,
                                                    supplier,
                                                    encoder,
                                                    errorHandler,
                                                    onFailureRespond,
                                                    responseDeliveryOptions,
                                                    retryCount,
                                                    timeout, delay, circuitBreakerTimeout) ->
                new ExecuteEventbusByteResponse(methodId,
                        vertx, t,
                        errorMethodHandler,
                        requestMessage,
                        supplier,
                        null,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout, delay,
                        circuitBreakerTimeout).execute();


        final ExecuteEventbusByteCallBlocking excecuteEventBusAndReply = (methodId,
                                                                          vertx,
                                                                          errorMethodHandler,
                                                                          requestMessage,
                                                                          errorHandler,
                                                                          errorHandlerByte,
                                                                          responseDeliveryOptions,
                                                                          retryCount, timeout,
                                                                          delay, circuitBreakerTimeout) ->
                EventbusBridgeBlockingExecution.sendMessageAndSupplyHandler(methodId,
                        _targetId,
                        _message,
                        _byteFunction,
                        deliveryOptions,
                        vertx,
                        errorMethodHandler,
                        requestMessage,
                        null,
                        errorHandler,
                        errorHandlerByte,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        delay,
                        circuitBreakerTimeout,
                        executor,
                        retry);


        return new ExecuteEventbusByteResponse(_methodId,
                _vertx,
                _failure,
                _errorMethodHandler,
                _requestMessage,
                _byteSupplier,
                excecuteEventBusAndReply,
                _errorHandler,
                _onFailureRespond,
                _responseDeliveryOptions,
                _retryCount,
                _timeout,
                _delay,
                _circuitBreakerTimeout);
    }

}
