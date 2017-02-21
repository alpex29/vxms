package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureBiConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.eventbus.basic.EventbusExecution;
import org.jacpfx.vertx.rest.interfaces.basic.ExecuteEventbusByteCall;
import org.jacpfx.vertx.rest.interfaces.basic.RecursiveExecutor;
import org.jacpfx.vertx.rest.interfaces.basic.RetryExecutor;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByteResponse;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicStringResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 05.04.16.
 * Typed execution of event-bus calls and byte response
 */
public class EventbusByteExecutionUtil {
    /**
     * create execution chain for event-bus request and reply to rest
     *
     * @param _methodId           the method identifier
     * @param _targetId           the event-bus target id
     * @param _message            the message to send
     * @param _bytefunction       the function to process the result message
     * @param _options            the event-bus delivery options
     * @param _vertx              the vertx instance
     * @param _failure            the failure thrown while task execution
     * @param _errorMethodHandler the error handler
     * @param _context            the vertx routing context
     * @return the execution chain {@link ExecuteRSBasicStringResponse}
     */
    public static ExecuteRSBasicByteResponse mapToByteResponse(String _methodId,
                                                               String _targetId,
                                                               Object _message,
                                                               ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> _bytefunction,
                                                               DeliveryOptions _options,
                                                               Vertx _vertx,
                                                               Throwable _failure,
                                                               Consumer<Throwable> _errorMethodHandler,
                                                               RoutingContext _context) {
        return mapToByteResponse(_methodId,
                _targetId,
                _message,
                _bytefunction,
                _options,
                _vertx,
                _failure,
                _errorMethodHandler,
                _context,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                0,
                0);
    }

    /**
     * create execution chain for event-bus request and reply to rest
     *
     * @param _methodId              the method identifier
     * @param _targetId              the event-bus target id
     * @param _message               the message to send
     * @param _byteFunction          the function to process the result message
     * @param _options               the event-bus delivery options
     * @param _vertx                 the vertx instance
     * @param _failure               the failure thrown while task execution
     * @param _errorMethodHandler    the error-method handler
     * @param _context               the vertx routing context
     * @param _headers               the headers to pass to the response
     * @param _byteConsumer          the consumer that takes a Future to complete, producing the byte response
     * @param _encoder               the encoder to encode your objects
     * @param _errorHandler          the error handler
     * @param _onFailureRespond      the consumer that takes a Future with the alternate response value in case of failure
     * @param _httpStatusCode        the http status code to set for response
     * @param _httpErrorCode         the http error code to set in case of failure handling
     * @param _retryCount            the amount of retries before failure execution is triggered
     * @param _timeout               the amount of time before the execution will be aborted
     * @param _circuitBreakerTimeout the amount of time before the circuit breaker closed again
     * @return the execution chain {@link ExecuteRSBasicStringResponse}
     */
    public static ExecuteRSBasicByteResponse mapToByteResponse(String _methodId,
                                                               String _targetId,
                                                               Object _message,
                                                               ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, byte[]> _byteFunction,
                                                               DeliveryOptions _options,
                                                               Vertx _vertx,
                                                               Throwable _failure,
                                                               Consumer<Throwable> _errorMethodHandler,
                                                               RoutingContext _context,
                                                               Map<String, String> _headers,
                                                               ThrowableFutureConsumer<byte[]> _byteConsumer,
                                                               Encoder _encoder,
                                                               Consumer<Throwable> _errorHandler,
                                                               ThrowableErrorConsumer<Throwable, byte[]> _onFailureRespond,
                                                               int _httpStatusCode,
                                                               int _httpErrorCode,
                                                               int _retryCount,
                                                               long _timeout,
                                                               long _circuitBreakerTimeout) {

        final DeliveryOptions _deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());


        final RetryExecutor retry = (methodId,
                                     id,
                                     message,
                                     byteFunction,
                                     deliveryOptions,
                                     vertx, t,
                                     errorMethodHandler,
                                     context,
                                     headers,
                                     encoder,
                                     errorHandler,
                                     onFailureRespond,
                                     httpStatusCode,
                                     httpErrorCode, retryCount,
                                     timeout, circuitBreakerTimeout) -> {
            final int decrementedCount = retryCount - 1;
            mapToByteResponse(methodId,
                    id, message,
                    byteFunction,
                    deliveryOptions,
                    vertx, t,
                    errorMethodHandler,
                    context, headers,
                    null,
                    encoder,
                    errorHandler,
                    onFailureRespond,
                    httpStatusCode,
                    httpErrorCode,
                    decrementedCount,
                    timeout,
                    circuitBreakerTimeout).
                    execute();
        };


        final RecursiveExecutor executor = (methodId,
                                            vertx,
                                            t,
                                            errorMethodHandler,
                                            context,
                                            headers,
                                            stringConsumer,
                                            excecuteEventBusAndReply,
                                            encoder,
                                            errorHandler,
                                            onFailureRespond,
                                            httpStatusCode, httpErrorCode,
                                            retryCount, timeout, circuitBreakerTimeout) ->
                new ExecuteRSBasicByteResponse(methodId,
                        vertx, t,
                        errorMethodHandler,
                        context, headers,
                        stringConsumer,
                        null,
                        encoder, errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount, timeout,
                        circuitBreakerTimeout).
                        execute();


        final ExecuteEventbusByteCall excecuteEventBusAndReply = (vertx,
                                                                  t,
                                                                  errorMethodHandler,
                                                                  context, headers,
                                                                  encoder, errorHandler,
                                                                  onFailureRespond,
                                                                  httpStatusCode,
                                                                  httpErrorCode,
                                                                  retryCount,
                                                                  timeout, circuitBreakerTimeout) ->
                EventbusExecution.sendMessageAndSupplyHandler(_methodId,
                        _targetId,
                        _message,
                        _byteFunction,
                        _deliveryOptions,
                        vertx,
                        t, errorMethodHandler,
                        context, headers,
                        encoder, errorHandler,
                        onFailureRespond,
                        httpStatusCode,
                        httpErrorCode,
                        retryCount,
                        timeout,
                        circuitBreakerTimeout, executor, retry);

        return new ExecuteRSBasicByteResponse(_methodId, _vertx, _failure, _errorMethodHandler, _context, _headers, _byteConsumer, excecuteEventBusAndReply, _encoder, _errorHandler,
                _onFailureRespond, _httpStatusCode, _httpErrorCode, _retryCount, _timeout, _circuitBreakerTimeout);
    }


}
