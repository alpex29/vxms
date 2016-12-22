package org.jacpfx.vertx.rest.response.basic;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicByteCircuitBreaker extends ExecuteRSBasicByteResponse {


    public ExecuteRSBasicByteCircuitBreaker(String methodId, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<byte[]> byteConsumer, ExecuteEventBusByteCall excecuteEventBusAndReply, Encoder encoder,
                                            Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond, int httpStatusCode, int httpErrorCode, int retryCount, long timeout, long circuitBreakerTimeout) {
        super(methodId, vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }





    /**
     * Define a timeout to release the stateful circuit breaker. Depending on your configuration the CircuitBreaker locks either cluster wide, jvm wide or only for the instance
     *
     * @param circuitBreakerTimeout the amount of time in ms before close the CircuitBreaker to allow "normal" execution path again, a value of 0l will use a stateless retry mechanism (performs faster)
     * @return the response chain
     */
    public ExecuteRSBasicByteResponse closeCircuitBreaker(long circuitBreakerTimeout) {
        return new ExecuteRSBasicByteResponse(methodId, vertx, t, errorMethodHandler, context, headers, byteConsumer, excecuteEventBusAndReply, encoder, errorHandler,
                onFailureRespond, httpStatusCode, httpErrorCode, retryCount, timeout, circuitBreakerTimeout);
    }


}
