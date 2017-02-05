package org.jacpfx.vertx.rest.interfaces;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.encoder.Encoder;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.03.16.
 */
@FunctionalInterface
public interface ExecuteEventBusStringCallAsync {

    void execute(Vertx vertx,
                 Throwable t,
                 Consumer<Throwable> errorMethodHandler,
                 RoutingContext context,
                 Map<String, String> headers, Encoder encoder,
                 Consumer<Throwable> errorHandler,
                 ThrowableFunction<Throwable, String> onFailureRespond,
                 int httpStatusCode, int httpErrorCode,
                 int retryCount, long timeout,
                 long delay, long circuitBreakerTimeout);
}
