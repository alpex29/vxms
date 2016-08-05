package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.vertx.rest.interfaces.ExecuteEventBusStringCall;
import org.jacpfx.vertx.rest.util.RESTExecutionUtil;
import org.jacpfx.vertx.rest.util.ResponseUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    protected final ThrowableSupplier<String> stringSupplier;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final Function<Throwable, String> onFailureRespond;
    protected final int httpStatusCode;
    protected final int retryCount;
    protected final ExecuteEventBusStringCall excecuteEventBusAndReply;


    public ExecuteRSBasicString(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableSupplier<String> stringSupplier, ExecuteEventBusStringCall excecuteEventBusAndReply, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.stringSupplier = stringSupplier;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.retryCount = retryCount;
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Execute the reply chain with given http status code
     *
     * @param status, the http status code
     */
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        new ExecuteRSBasicString(vertx, t, errorMethodHandler, context, headers, stringSupplier,
                excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount).execute();
    }

    /**
     * Execute the reply chain with given http status code and content-type
     *
     * @param status,     the http status code
     * @param contentType , the html content-type
     */
    public void execute(HttpResponseStatus status, String contentType) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(contentType);
        new ExecuteRSBasicString(vertx, t, errorMethodHandler, context, updateContentType(contentType),
                stringSupplier, excecuteEventBusAndReply, encoder, errorHandler,
                onFailureRespond, status.code(), retryCount).execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        new ExecuteRSBasicString(vertx, t, errorMethodHandler, context,
                updateContentType(contentType), stringSupplier, excecuteEventBusAndReply,
                encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount).execute();
    }

    /**
     * Execute the reply chain
     */
    public void execute() {
        vertx.runOnContext(action -> {
            // excecuteEventBusAndReply & stringSupplier never non null at the same time
            Optional.ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
                try {
                    evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount);
                } catch (Exception e) {
                    System.err.println("EXCEPTION ::::::");
                    e.printStackTrace();
                }

            });

            Optional.ofNullable(stringSupplier).
                    ifPresent(supplier -> {
                                int retry = retryCount;
                                String result = null;
                                Optional.
                                        ofNullable(ResponseUtil.
                                                createResponse(retry, result, supplier, errorHandler, onFailureRespond, errorMethodHandler)).
                                        ifPresent(res -> repond(res));

                                checkAndCloseResponse(retry);

                            }
                    );

        });


    }

    protected void checkAndCloseResponse(int retry) {
        final HttpServerResponse response = context.response();
        if (retry == 0 && !response.ended()) {
            response.end();
        }
    }

    protected void repond(String result) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            RESTExecutionUtil.updateResponseHaders(headers, response);
            RESTExecutionUtil.updateResponseStatusCode(httpStatusCode, response);
            if (result != null) {
                response.end(result);
            } else {
                response.end();
            }
        }
    }

    protected Map<String, String> updateContentType(String contentType) {
        Map<String, String> headerMap = new HashMap<>(headers);
        headerMap.put("content-type", contentType);
        return headerMap;
    }

}
