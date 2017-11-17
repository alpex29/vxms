/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.event.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.Optional;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;
import org.jacpfx.vxms.event.eventbus.blocking.EventbusBridgeBlockingExecution;
import org.jacpfx.vxms.event.interfaces.blocking.ExecuteEventbusByteCallBlocking;
import org.jacpfx.vxms.event.interfaces.blocking.RecursiveBlockingExecutor;
import org.jacpfx.vxms.event.interfaces.blocking.RetryBlockingExecutor;
import org.jacpfx.vxms.event.response.blocking.ExecuteEventbusByteResponse;

/**
 * Created by Andy Moncsek on 05.04.16. Typed execution of event-bus calls and blocking object
 * response
 */
public class EventbusByteExecutionBlockingUtil {

  /**
   * create execution chain for event-bus request and reply to the event
   *
   * @param _methodId the method identifier
   * @param _targetId the event-bus target id
   * @param _message the message to send
   * @param _byteFunction the function to process the result message
   * @param _requestDeliveryOptions the event-bus delivery serverOptions
   * @param _vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param _failure the failure thrown while task execution
   * @param _errorMethodHandler the error-method handler
   * @param _requestMessage the event-bus request serverOptions
   * @param _byteSupplier the supplier, producing the byte response
   * @param _errorHandler the error handler
   * @param _onFailureRespond the consumer that takes a Future with the alternate response value in
   *     case of failure
   * @param _responseDeliveryOptions the response delivery serverOptions
   * @param _retryCount the amount of retries before failure execution is triggered
   * @param _timeout the amount of time before the execution will be aborted
   * @param _delay the delay between an error and the retry
   * @param _circuitBreakerTimeout the amount of time before the circuit breaker closed again
   * @return the execution chain {@link ExecuteEventbusByteResponse}
   */
  public static ExecuteEventbusByteResponse mapToByteResponse(
      String _methodId,
      String _targetId,
      Object _message,
      ThrowableFunction<AsyncResult<Message<Object>>, byte[]> _byteFunction,
      DeliveryOptions _requestDeliveryOptions,
      VxmsShared _vxmsShared,
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

    final DeliveryOptions deliveryOptions =
        Optional.ofNullable(_requestDeliveryOptions).orElse(new DeliveryOptions());

    final RetryBlockingExecutor retry =
        (methodId,
            targetId,
            message,
            function,
            requestDeliveryOptions,
            vxmsShared,
            t,
            errorMethodHandler,
            requestMessage,
            supplier,
            encoder,
            errorHandler,
            onFailureRespond,
            responseDeliveryOptions,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout) -> {
          int retryValue = retryCount - 1;
          mapToByteResponse(
                  methodId,
                  targetId,
                  message,
                  function,
                  requestDeliveryOptions,
                  vxmsShared,
                  t,
                  errorMethodHandler,
                  requestMessage,
                  null,
                  errorHandler,
                  onFailureRespond,
                  responseDeliveryOptions,
                  retryValue,
                  timeout,
                  delay,
                  circuitBreakerTimeout)
              .execute();
        };

    final RecursiveBlockingExecutor executor =
        (methodId,
            vxmsShared,
            t,
            errorMethodHandler,
            requestMessage,
            supplier,
            encoder,
            errorHandler,
            onFailureRespond,
            responseDeliveryOptions,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout) ->
            new ExecuteEventbusByteResponse(
                    methodId,
                    vxmsShared,
                    t,
                    errorMethodHandler,
                    requestMessage,
                    null,
                    supplier,
                    null,
                    errorHandler,
                    onFailureRespond,
                    responseDeliveryOptions,
                    retryCount,
                    timeout,
                    delay,
                    circuitBreakerTimeout)
                .execute();

    final ExecuteEventbusByteCallBlocking excecuteEventBusAndReply =
        (methodId,
            vertx,
            errorMethodHandler,
            requestMessage,
            errorHandler,
            errorHandlerByte,
            responseDeliveryOptions,
            retryCount,
            timeout,
            delay,
            circuitBreakerTimeout) ->
            EventbusBridgeBlockingExecution.sendMessageAndSupplyHandler(
                methodId,
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

    return new ExecuteEventbusByteResponse(
        _methodId,
        _vxmsShared,
        _failure,
        _errorMethodHandler,
        _requestMessage,
        null,
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
