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

package org.jacpfx.vertx.event.interfaces.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.throwable.ThrowableErrorConsumer;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.03.16.
 * Typed functional interface called on event-bus response. The execution will be handled as non-blocking code.
 */
@FunctionalInterface
public interface ExecuteEventbusStringCall {
    /**
     * Execute typed execution handling
     *
     * @param methodId                the method identifier
     * @param vertx                   the vertx instance
     * @param errorMethodHandler      the error-method handler
     * @param requestMessage          the message to responde to
     * @param errorHandler            the error handler
     * @param onFailureRespond        the consumer that takes a Future with the alternate response value in case of failure
     * @param responseDeliveryOptions the delivery options for the response
     * @param retryCount              the amount of retries before failure execution is triggered
     * @param timeout                 the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout   the amount of time before the circuit breaker closed again
     */
    void execute(String methodId,
                 Vertx vertx,
                 Consumer<Throwable> errorMethodHandler,
                 Message<Object> requestMessage,
                 Consumer<Throwable> errorHandler,
                 ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                 DeliveryOptions responseDeliveryOptions,
                 int retryCount, long timeout, long circuitBreakerTimeout);
}
