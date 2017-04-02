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

package org.jacpfx.vertx.event.response.blocking;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.throwable.ThrowableFunction;
import org.jacpfx.common.throwable.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.event.interfaces.blocking.ExecuteEventbusObjectCallBlocking;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 * This class defines the fluid API part to define the amount of time after the circuit breaker will be closed again
 */
public class ExecuteEventbusObjectCircuitBreaker extends ExecuteEventbusObjectResponse {

    /**
     * The constructor to pass all needed members
     *
     * @param methodId                 the method identifier
     * @param vertx                    the vertx instance
     * @param failure                  the failure thrown while task execution
     * @param errorMethodHandler       the error handler
     * @param message                  the message to responde to
     * @param objectSupplier           the supplier, producing the byte response
     * @param excecuteEventBusAndReply the response of an event-bus call which is passed to the fluent API
     * @param encoder                  the encoder to serialize your object
     * @param errorHandler             the error handler
     * @param onFailureRespond         the consumer that takes a Future with the alternate response value in case of failure
     * @param deliveryOptions          the response delivery options
     * @param retryCount               the amount of retries before failure execution is triggered
     * @param timeout                  the amount of time before the execution will be aborted
     * @param delay                    the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout    the amount of time before the circuit breaker closed again
     */
    public ExecuteEventbusObjectCircuitBreaker(String methodId,
                                               Vertx vertx,
                                               Throwable failure,
                                               Consumer<Throwable> errorMethodHandler,
                                               Message<Object> message,
                                               ThrowableSupplier<Serializable> objectSupplier,
                                               ExecuteEventbusObjectCallBlocking excecuteEventBusAndReply,
                                               Encoder encoder,
                                               Consumer<Throwable> errorHandler,
                                               ThrowableFunction<Throwable, Serializable> onFailureRespond,
                                               DeliveryOptions deliveryOptions,
                                               int retryCount,
                                               long timeout,
                                               long delay,
                                               long circuitBreakerTimeout){
        super(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                objectSupplier,
                excecuteEventBusAndReply,
                encoder, errorHandler,
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
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain  {@link ExecuteEventbusObjectResponse}
     */
    public ExecuteEventbusObjectResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteEventbusObjectResponse(methodId,
                vertx,
                failure,
                errorMethodHandler,
                message,
                objectSupplier,
                excecuteEventBusAndReply,
                encoder,
                errorHandler,
                onFailureRespond,
                deliveryOptions,
                retryCount,
                timeout,
                delay,
                circuitBreakerTimeout);
    }


}
