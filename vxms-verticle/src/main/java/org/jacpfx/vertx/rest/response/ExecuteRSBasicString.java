package org.jacpfx.vertx.rest.response;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.util.RESTExecutionHandler;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSBasicString {
    protected final Vertx vertx;
    protected final Throwable t;
    protected final Consumer<Throwable> errorMethodHandler;
    protected final RoutingContext context;
    protected final Map<String, String> headers;
    protected final boolean async;
    protected final ThrowableSupplier<String> stringSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Function<Throwable, String> errorHandlerString;
    protected final int httpStatusCode;
    protected final int retryCount;


    public ExecuteRSBasicString(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<String> stringSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> errorHandlerString, int httpStatusCode, int retryCount) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.async = async;
        this.stringSupplier = stringSupplier;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.errorHandlerString = errorHandlerString;
        this.retryCount = retryCount;
        this.httpStatusCode = httpStatusCode;
    }

    public void execute(HttpResponseStatus status) {
        final ExecuteRSBasicString lastStep = new ExecuteRSBasicString(vertx, t, errorMethodHandler, context, headers, async, stringSupplier, encoder, errorHandler, errorHandlerString, status.code(), retryCount);
        lastStep.execute();
    }

    public void execute() {

        Optional.ofNullable(stringSupplier).
                ifPresent(supplier -> {
                            int retry = retryCount;
                            String result = null;
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();
                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = RESTExecutionHandler.handleError(context.response(), result, errorHandler, errorHandlerString, errorMethodHandler, e);
                                    } else {
                                        RESTExecutionHandler.handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended()) {
                                updateResponseHaders();
                                HttpServerResponse response = getHttpServerResponse();
                                if (result != null) {
                                    response.end(result);
                                } else {
                                    response.end();
                                }
                            }

                        }
                );


    }

    private HttpServerResponse getHttpServerResponse() {
        HttpServerResponse response = context.response();
        if (httpStatusCode != 0) {
            response = response.setStatusCode(httpStatusCode);
        }
        return response;
    }

    protected void updateResponseHaders() {
        Optional.ofNullable(headers).ifPresent(h -> h.entrySet().stream().forEach(entry -> context.response().putHeader(entry.getKey(), entry.getValue())));
    }

}
