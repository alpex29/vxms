package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableFutureConsumer;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusByteCall;
import org.jacpfx.vertx.rest.response.basic.ExecuteRSBasicByteResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusByteExecutionUtil {

    public static ExecuteRSBasicByteResponse mapToByteResponse(String _id, Object _message, DeliveryOptions _options, Function<AsyncResult<Message<Object>>, ?> _errorFunction,
                                                               ThrowableFunction<AsyncResult<Message<Object>>, byte[]> _byteFunction, Vertx _vertx, Throwable _t,
                                                               Consumer<Throwable> _errorMethodHandler, RoutingContext _context, Map<String, String> _headers,
                                                               ThrowableFutureConsumer<byte[]> _byteSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                               ThrowableErrorConsumer<Throwable, byte[]> _onFailureRespond, int _httpStatusCode, int _retryCount, long _timeout) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusByteCall excecuteEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                  context, headers,
                                                                  encoder, errorHandler, onFailureRespond,
                                                                  httpStatusCode, retryCount, timeout) ->
                sendMessageAndSupplyByteHandler(_id, _message, _options, _errorFunction, _byteFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);


        return new ExecuteRSBasicByteResponse(_vertx, _t, _errorMethodHandler, _context, _headers, _byteSupplier, excecuteEventBusAndReply, _encoder, _errorHandler, _onFailureRespond, _httpStatusCode, _retryCount, _timeout);
    }

    private static void sendMessageAndSupplyByteHandler(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                        ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                        Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                        Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                                        int httpStatusCode, int retryCount, long timeout) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createByteSupplierAndExecute(id, message, options, errorFunction, byteFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, onFailureRespond, httpStatusCode,
                                        retryCount, timeout, event));
    }

    private static void createByteSupplierAndExecute(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                     ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                     Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                     Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                                     int httpStatusCode, int retryCount, long timeout, AsyncResult<Message<Object>> event) {
        final ThrowableFutureConsumer<byte[]> byteSupplier = createByteSupplier(id, message, options, errorFunction, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout, event);

        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSBasicByteResponse(vertx, t, errorMethodHandler, context, headers, byteSupplier, null, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout).execute();
        } else if (event.failed() && retryCount > 0) {
            retryByteOperation(id, message, options, errorFunction, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
        }
    }

    private static void retryByteOperation(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                           ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                           Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                           Encoder encoder, Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                           int httpStatusCode, int retryCount, long timeout) {
        mapToByteResponse(id, message, options, errorFunction, byteFunction, vertx, t, errorMethodHandler,
                context, headers, null, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount - 1, timeout).
                execute();
    }


    private static ThrowableFutureConsumer<byte[]> createByteSupplier(String id, Object message, DeliveryOptions options, Function<AsyncResult<Message<Object>>, ?> errorFunction,
                                                                      ThrowableFunction<AsyncResult<Message<Object>>, byte[]> byteFunction, Vertx vertx, Throwable t,
                                                                      Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers,
                                                                      Encoder encoder,Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, byte[]> onFailureRespond,
                                                                      int httpStatusCode, int retryCount, long timeout, AsyncResult<Message<Object>> event) {
        return (future) -> {
            byte[] resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryByteOperation(id, message, options, errorFunction, byteFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
                } else {
                    resp = (byte[]) executeErrorFunction(event, errorFunction);
                }
            } else {
                resp = byteFunction.apply(event);
            }

            future.complete(resp);
        };
    }


    private static Object executeErrorFunction(AsyncResult<Message<Object>> event, Function<AsyncResult<Message<Object>>, ?> errorFunction) throws Throwable {
        Object resp;
        final Optional<? extends Function<AsyncResult<Message<Object>>, ?>> ef = Optional.ofNullable(errorFunction);
        if (!ef.isPresent()) throw event.cause();
        final Function<AsyncResult<Message<Object>>, ?> localErrorFunction = ef.get();
        resp = localErrorFunction.apply(event);
        return resp;
    }
}
