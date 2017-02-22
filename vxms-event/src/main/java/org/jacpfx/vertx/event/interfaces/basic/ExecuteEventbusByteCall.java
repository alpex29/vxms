package org.jacpfx.vertx.event.interfaces.basic;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.jacpfx.common.ThrowableErrorConsumer;

import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 21.03.16.
 */
@FunctionalInterface
public interface ExecuteEventbusByteCall {

    void execute(String methodId,
                 Vertx vertx,
                 Consumer<Throwable> errorMethodHandler,
                 Message<Object> message,
                 Consumer<Throwable> errorHandler,
                 ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                 DeliveryOptions deliveryOptions,
                 int retryCount,
                 long timeout,
                 long circuitBreakerTimeout);
}
