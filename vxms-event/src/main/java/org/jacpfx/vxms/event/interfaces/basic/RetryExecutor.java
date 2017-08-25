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

package org.jacpfx.vxms.event.interfaces.basic;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;

/**
 * Created by amo on 31.01.17.
 * Generic Functional interface to pass typed executions steps in case of retry operations
 */
@FunctionalInterface
public interface RetryExecutor<T> {

  /**
   * Execute typed retry handling
   *
   * @param targetId event-bus target id
   * @param message the message to reply
   * @param function the function to execute and to create a response object
   * @param requestDeliveryOptions the request delivery options
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param failure the failure thrown while task execution or messaging
   * @param errorMethodHandler the error-method handler
   * @param requestMessage the message to reply to
   * @param consumer the consumer to complete the response
   * @param encoder the encoder to serialize the response object
   * @param errorHandler the error handler
   * @param onFailureRespond the consumer that takes a Future with the alternate response value in
   * case of failure
   * @param responseDeliveryOptions the delivery options for the response
   * @param retryCount the amount of retries before failure execution is triggered
   * @param timeout the delay time in ms between an execution error and the retry
   * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
   */
  void execute(String targetId,
      Object message,
      ThrowableFutureBiConsumer<AsyncResult<Message<Object>>, T> function,
      DeliveryOptions requestDeliveryOptions,
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> requestMessage,
      ThrowableFutureConsumer<T> consumer,
      Encoder encoder,
      Consumer<Throwable> errorHandler,
      ThrowableErrorConsumer<Throwable, T> onFailureRespond,
      DeliveryOptions responseDeliveryOptions,
      int retryCount, long timeout, long circuitBreakerTimeout);
}