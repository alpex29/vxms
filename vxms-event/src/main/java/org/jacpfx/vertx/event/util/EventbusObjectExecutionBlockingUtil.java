package org.jacpfx.vertx.event.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.eventbus.blocking.EventbusBlockingExecution;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusObjectCallBlocking;
import org.jacpfx.vertx.event.interfaces.blocking.RecursiveBlockingExecutor;
import org.jacpfx.vertx.event.interfaces.blocking.RetryBlockingExecutor;
import org.jacpfx.vertx.event.response.blocking.ExecuteRSObjectResponse;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Typed execution of event-bus calls and blocking object response
 */
public class EventbusObjectExecutionBlockingUtil {
    /**
     * create execution chain for event-bus request and reply to the event
     *
     * @param _methodId               the method identifier
     * @param _targetId               the event-bus target id
     * @param _message                the message to send
     * @param _objectFunction         the function to process the result message
     * @param _requestDeliveryOptions the event-bus delivery options
     * @param _vertx                  the vertx instance
     * @param _failure                the failure thrown while task execution
     * @param _errorMethodHandler     the error-method handler
     * @param _requestMessage         the event-bus request options
     * @param _objectSupplier         the supplier, producing the object response
     * @param _encoder                the encoder to serialize the result object
     * @param _errorHandler           the error handler
     * @param _onFailureRespond       the consumer that takes a Future with the alternate response value in case of failure
     * @param _retryCount             the amount of retries before failure execution is triggered
     * @param _timeout                the amount of time before the execution will be aborted
     * @param _delay                  the delay between an error and the retry
     * @param _circuitBreakerTimeout  the amount of time before the circuit breaker closed again
     * @return the execution chain {@link ExecuteRSObjectResponse}
     */
    public static ExecuteRSObjectResponse mapToObjectResponse(String _methodId,
                                                              String _targetId,
                                                              Object _message,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, Serializable> _objectFunction,
                                                              DeliveryOptions _requestDeliveryOptions,
                                                              Vertx _vertx,
                                                              Throwable _failure,
                                                              Consumer<Throwable> _errorMethodHandler,
                                                              Message<Object> _requestMessage,
                                                              ThrowableSupplier<Serializable> _objectSupplier,
                                                              Encoder _encoder,
                                                              Consumer<Throwable> _errorHandler,
                                                              ThrowableFunction<Throwable, Serializable> _onFailureRespond,
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
            mapToObjectResponse(methodId,
                    targetId,
                    message, function,
                    requestDeliveryOptions,
                    vertx,
                    t,
                    errorMethodHandler,
                    requestMessage,
                    null,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryValue,
                    timeout,
                    delay, circuitBreakerTimeout).
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
                new ExecuteRSObjectResponse(methodId,
                        vertx, t,
                        errorMethodHandler,
                        requestMessage,
                        supplier,
                        null,
                        encoder,
                        errorHandler,
                        onFailureRespond,
                        responseDeliveryOptions,
                        retryCount,
                        timeout, delay,
                        circuitBreakerTimeout).execute();


        final ExecuteEventbusObjectCallBlocking excecuteEventBusAndReply = (methodId,
                                                                            vertx,
                                                                            errorMethodHandler,
                                                                            requestMessage,
                                                                            encoder,
                                                                            errorHandler,
                                                                            errorHandlerObject,
                                                                            responseDeliveryOptions,
                                                                            retryCount, timeout,
                                                                            delay, circuitBreakerTimeout) ->
                EventbusBlockingExecution.sendMessageAndSupplyHandler(
                        methodId,
                        _targetId,
                        _message,
                        _objectFunction,
                        deliveryOptions,
                        vertx,
                        errorMethodHandler,
                        requestMessage,
                        encoder,
                        errorHandler,
                        errorHandlerObject,
                        responseDeliveryOptions,
                        retryCount,
                        timeout,
                        delay,
                        circuitBreakerTimeout,
                        executor,
                        retry);


        return new ExecuteRSObjectResponse(_methodId,
                _vertx,
                _failure,
                _errorMethodHandler,
                _requestMessage,
                _objectSupplier,
                excecuteEventBusAndReply,
                _encoder,
                _errorHandler,
                _onFailureRespond,
                _responseDeliveryOptions,
                _retryCount,
                _timeout,
                _delay,
                _circuitBreakerTimeout);
    }


}
