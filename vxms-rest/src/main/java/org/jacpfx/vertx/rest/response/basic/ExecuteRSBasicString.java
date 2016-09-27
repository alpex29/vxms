package org.jacpfx.vertx.rest.response.basic;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableErrorConsumer;
import org.jacpfx.common.ThrowableFutureConsumer;
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
    protected final ThrowableFutureConsumer<String> stringConsumer;
    protected final Encoder encoder;
    protected final Consumer<Throwable> errorHandler;
    protected final ThrowableErrorConsumer<Throwable, String> onFailureRespond;
    protected final ExecuteEventBusStringCall excecuteEventBusAndReply;
    protected final int httpStatusCode;
    protected final int retryCount;
    protected final long timeout;


    public ExecuteRSBasicString(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, ThrowableFutureConsumer<String> stringConsumer, ExecuteEventBusStringCall excecuteEventBusAndReply, Encoder encoder,
                                Consumer<Throwable> errorHandler, ThrowableErrorConsumer<Throwable, String> onFailureRespond, int httpStatusCode, int retryCount,long timeout) {
        this.vertx = vertx;
        this.t = t;
        this.errorMethodHandler = errorMethodHandler;
        this.context = context;
        this.headers = headers;
        this.stringConsumer = stringConsumer;
        this.excecuteEventBusAndReply = excecuteEventBusAndReply;
        this.encoder = encoder;
        this.errorHandler = errorHandler;
        this.onFailureRespond = onFailureRespond;
        this.retryCount = retryCount;
        this.httpStatusCode = httpStatusCode;
        this.timeout = timeout;
    }

    /**
     * Execute the reply chain with given http status code
     *
     * @param status, the http status code
     */
    public void execute(HttpResponseStatus status) {
        Objects.requireNonNull(status);
        new ExecuteRSBasicString(vertx, t, errorMethodHandler, context, headers, stringConsumer,
                excecuteEventBusAndReply, encoder, errorHandler, onFailureRespond, status.code(), retryCount, timeout).execute();
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
                stringConsumer, excecuteEventBusAndReply, encoder, errorHandler,
                onFailureRespond, status.code(), retryCount,timeout).execute();
    }

    /**
     * Executes the reply chain whith given html content-type
     *
     * @param contentType, the html content-type
     */
    public void execute(String contentType) {
        Objects.requireNonNull(contentType);
        new ExecuteRSBasicString(vertx, t, errorMethodHandler, context,
                updateContentType(contentType), stringConsumer, excecuteEventBusAndReply,
                encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount,timeout).execute();
    }

    /**
     * Execute the reply chain
     */
    public void execute() {
        // TODO timeout should trac eventbus call and mapToString together !!!!
        vertx.runOnContext(action -> {
            // excecuteEventBusAndReply & stringSupplier never non null at the same time
            Optional.ofNullable(excecuteEventBusAndReply).ifPresent(evFunction -> {
                try {
                    evFunction.execute(vertx, t, errorMethodHandler, context, headers, encoder, errorHandler, onFailureRespond, httpStatusCode, retryCount, timeout);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            Optional.ofNullable(stringConsumer).
                    ifPresent(userOperation -> {
                                int retry = retryCount;
                                ResponseUtil.createResponse(retry,timeout, userOperation, errorHandler, onFailureRespond, errorMethodHandler,vertx, value -> {
                                    if(value.succeeded()) {
                                        respond(value.getResult());
                                    } else {
                                        respond(value.getCause().getMessage(),HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                                    }
                                    checkAndCloseResponse(retry);
                                });

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

    protected void respond(String result) {
        respond(result,httpStatusCode);
    }

    protected void respond(String result, int statuscode) {
        final HttpServerResponse response = context.response();
        if (!response.ended()) {
            RESTExecutionUtil.updateResponseHaders(headers, response);
            RESTExecutionUtil.updateResponseStatusCode(statuscode, response);
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
