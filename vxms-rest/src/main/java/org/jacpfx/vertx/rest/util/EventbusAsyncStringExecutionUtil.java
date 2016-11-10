package org.jacpfx.vertx.rest.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableFunction;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCallAsync;
import org.jacpfx.vertx.rest.response.blocking.ExecuteRSStringResponse;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 05.04.16.
 */
public class EventbusAsyncStringExecutionUtil {


    public static ExecuteRSStringResponse mapToStringResponse(String _methodId,String _id, Object _message, DeliveryOptions _options,
                                                              ThrowableFunction<AsyncResult<Message<Object>>, String> _stringFunction, Vertx _vertx, Throwable _t, Consumer<Throwable> _errorMethodHandler,
                                                              RoutingContext _context, Map<String, String> _headers, ThrowableSupplier<String> _stringSupplier, Encoder _encoder, Consumer<Throwable> _errorHandler,
                                                              Function<Throwable, String> _errorHandlerString, int _httpStatusCode, int _retryCount, long _timeout, long _delay) {

        final DeliveryOptions deliveryOptions = Optional.ofNullable(_options).orElse(new DeliveryOptions());
        final ExecuteEventBusStringCallAsync excecuteAsyncEventBusAndReply = (vertx, t, errorMethodHandler,
                                                                              context, headers,
                                                                              encoder, errorHandler, errorHandlerString,
                                                                              httpStatusCode, retryCount, timeout, delay) ->
                sendMessageAndSupplyStringHandler(_methodId,_id, _message, _options, _stringFunction, deliveryOptions, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, timeout, delay);

        return new ExecuteRSStringResponse(_methodId,_vertx, _t, _errorMethodHandler, _context, _headers, _stringSupplier,
                excecuteAsyncEventBusAndReply, _encoder, _errorHandler, _errorHandlerString, _httpStatusCode, _retryCount, _timeout, _delay);
    }

    private static void sendMessageAndSupplyStringHandler(String methodId,String id, Object message, DeliveryOptions options,
                                                          ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, DeliveryOptions deliveryOptions, Vertx vertx, Throwable t,
                                                          Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler,
                                                          Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, long timeout, long delay) {
        vertx.
                eventBus().
                send(id, message, deliveryOptions,
                        event ->
                                createStringSupplierAndExecute(methodId,id, message, options, stringFunction,
                                        vertx, t, errorMethodHandler,
                                        context, headers, encoder,
                                        errorHandler, errorHandlerString, httpStatusCode,
                                        retryCount, timeout, delay, event));
    }

    private static void createStringSupplierAndExecute(String methodId,String id, Object message, DeliveryOptions options,
                                                       ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                       Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                       Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, long timeout, long delay, AsyncResult<Message<Object>> event) {
        final ThrowableSupplier<String> stringSupplier = createStringSupplier(methodId,id, message, options, stringFunction,
                vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, timeout, delay, event);
        if (!event.failed() || (event.failed() && retryCount <= 0)) {
            new ExecuteRSStringResponse(methodId,vertx, t, errorMethodHandler, context, headers, stringSupplier, null,
                    encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, timeout, delay).execute();
        } else if (event.failed() && retryCount > 0) {
            retryStringOperation(methodId,id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, timeout, delay);
        }
    }

    private static void retryStringOperation(String methodId,String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, long timeout, long delay) {
        mapToStringResponse(methodId,id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, null, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount - 1, timeout, delay).
                execute();
    }


    private static ThrowableSupplier<String> createStringSupplier(String methodId,String id, Object message, DeliveryOptions options, ThrowableFunction<AsyncResult<Message<Object>>, String> stringFunction, Vertx vertx, Throwable t,
                                                                  Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, Encoder encoder,
                                                                  Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount, long timeout, long delay, AsyncResult<Message<Object>> event) {
        return () -> {
            String resp = null;
            if (event.failed()) {
                if (retryCount > 0) {
                    retryStringOperation(methodId,id, message, options, stringFunction, vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, errorHandlerString, httpStatusCode, retryCount, timeout, delay);
                } else {
                    throw event.cause();
                }
            } else {
                resp = stringFunction.apply(event);
            }

            return resp;
        };
    }


}
