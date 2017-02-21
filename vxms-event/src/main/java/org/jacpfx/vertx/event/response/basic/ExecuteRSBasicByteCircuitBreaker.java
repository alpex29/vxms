package org.jacpfx.vertx.event.response.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.interfaces.basic.ExecuteEventbusByteCall;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicByteCircuitBreaker extends ExecuteRSBasicByteResponse {


    public ExecuteRSBasicByteCircuitBreaker(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, Message<Object> message, ThrowableFutureConsumer<byte[]> byteConsumer, ExecuteEventbusByteCall excecuteEventBusAndReply,
                                            Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, DeliveryOptions deliveryOptions, int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, message, byteConsumer, excecuteEventBusAndReply, errorHandler, onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }





    /**
     * Define a timeout to release the stateful circuit breaker. Depending on your configuration the CircuitBreaker locks either cluster wide, jvm wide or only for the instance
     *
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain
     */
    public ExecuteRSBasicByteResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteRSBasicByteResponse(methodId, vertx, t, errorMethodHandler, message, byteConsumer, excecuteEventBusAndReply,  errorHandler,
                onFailureRespond, deliveryOptions, retryCount, timeout, circuitBreakerTimeout);
    }


}
