package org.jacpfx.vertx.rest.interfaces.blocking;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by amo on 31.01.17.
 * Generic Functional interface for handling typed execution of fluid API
 */
@FunctionalInterface
public interface RecursiveBlockingExecutor<T> {
    /**
     * Execute typed execution handling
     *
     * @param methodId              the method identifier
     * @param vertx                 the vertx instance
     * @param failure               the failure thrown while task execution or messaging
     * @param errorMethodHandler    the error-method handler
     * @param context               the vertx routing context
     * @param headers               the headers to pass to the response
     * @param encoder               the encoder to encode your objects
     * @param errorHandler          the error handler
     * @param onFailureRespond      the consumer that takes a Future with the alternate response value in case of failure
     * @param httpStatusCode        the http status code to set for response
     * @param httpErrorCode         the http error code to set in case of failure handling
     * @param retryCount            the amount of retries before failure execution is triggered
     * @param timeout               the delay time in ms between an execution error and the retry
     * @param delay                 the delay time in ms between an execution error and the retry
     * @param circuitBreakerTimeout the amount of time before the circuit breaker closed again
     */
    void execute(String methodId,
                 Vertx vertx,
                 Throwable failure,
                 Consumer<Throwable> errorMethodHandler,
                 RoutingContext context,
                 Map<String, String> headers,
                 ThrowableSupplier<T> supplier,
                 Encoder encoder, Consumer<Throwable> errorHandler,
                 ThrowableFunction<Throwable, T> onFailureRespond,
                 int httpStatusCode, int httpErrorCode,
                 int retryCount, long timeout,
                 long delay,
                 long circuitBreakerTimeout);
}