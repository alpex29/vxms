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

package org.jacpfx.vertx.event.response.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.throwable.ThrowableErrorConsumer;
import org.jacpfx.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusStringCall;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API for String responses, defines access to failure handling, timeouts,...
 */
public class ExecuteEventbusBasicStringResponse extends ExecuteEventbusBasicString {


    /**
     * The constructor to pass all needed members
     *
     * @param methodId                 the method identifier
     * @param vertx                    the vertx instance
     * @param failure                  the failure thrown while task execution
     * @param errorMethodHandler       the error handler
     * @param message                  the message to responde to
     * @param stringConsumer           the consumer, producing the byte response
     * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param errorHandler             the error handler
     * @param onFailureRespond         the consumer that takes a Future with the alternate response value in case of failure
     * @param deliveryOptions          the response delivery options
     * @param retryCount               the amount of retries before failure execution is triggered
     * @param timeout                  the amount of time before the execution will be aborted
     * @param circuitBreakerTimeout    the amount of time before the circuit breaker closed again
     */
    public ExecuteEventbusBasicStringResponse(String methodId,
                                              Vertx vertx,
                                              Throwable failure,
                                              Consumer<Throwable> errorMethodHandler,
                                              Message<Object> message,
                                              ThrowableFutureConsumer<String> stringConsumer,
                                              ExecuteEventbusStringCall excecuteEventBusAndReply,
                                              Consumer<Throwable> errorHandler,
                                              ThrowableErrorConsumer<Throwable, String> onFailureRespond,
                                              DeliveryOptions deliveryOptions,
                                              int retryCount,
                                              long timeout,
                                              long circuitBreakerTimeout) {
        super(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringConsumer,
                excecuteEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }


    /**
     * intermediate error handler which will be called on each error (at least 1 time, in case on N retries... up to N times)
     *
     * @param errorHandler the handler to be executed on each error
     * @return the response chain {@link ExecuteEventbusBasicStringResponse}
     */
    public ExecuteEventbusBasicStringResponse onError(Consumer<Throwable> errorHandler) {
        return new ExecuteEventbusBasicStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringConsumer,
                excecuteEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }

    /**
     * Defines how long a method can be executed before aborted.
     *
     * @param timeout the amount of timeout in ms
     * @return the response chain {@link ExecuteEventbusBasicStringResponse}
     */
    public ExecuteEventbusBasicStringResponse timeout(long timeout) {
        return new ExecuteEventbusBasicStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringConsumer,
                excecuteEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }

    /**
     * retry execution N times before
     *
     * @param retryCount the amount of retries
     * @return the response chain {@link ExecuteEventbusBasicStringCircuitBreaker}
     */
    public ExecuteEventbusBasicStringCircuitBreaker retry(int retryCount) {
        return new ExecuteEventbusBasicStringCircuitBreaker(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringConsumer,
                excecuteEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }

    /**
     * defines an action for errors in byte responses, you can handle the error and return an alternate createResponse value, this handler is a terminal handler and will be executed only once
     *
     * @param onFailureRespond the handler (function) to execute on error
     * @return the response chain {@link ExecuteEventbusBasicStringResponse}
     */
    public ExecuteEventbusBasicStringResponse onFailureRespond(ThrowableErrorConsumer<Throwable, String> onFailureRespond) {
        return new ExecuteEventbusBasicStringResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                stringConsumer,
                excecuteEventBusAndReply,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                circuitBreakerTimeout);
    }


}
