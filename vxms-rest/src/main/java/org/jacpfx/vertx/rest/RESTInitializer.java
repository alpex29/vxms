/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vertx.rest;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.util.ConfigurationUtil;
import org.jacpfx.common.util.URIUtil;
import org.jacpfx.vertx.rest.annotation.OnRestError;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.rest.util.ReflectionUtil;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Andy Moncsek on 09.03.16.
 * Handles initialization of vxms rest module implementation
 */
public class RESTInitializer {



    public static final String HTTP_ALL = "ALL";

    /**
     * initialize default REST implementation for vxms
     *
     * @param vxmsShared   the vxmsShared instance, containing the Vertx instance and other shared objects per instance
     * @param router  the Router instance
     * @param service the Vxms service object itself
     */
    static void initRESTHandler(VxmsShared vxmsShared, Router router, Object service) {
        Stream.of(service.getClass().getDeclaredMethods()).
                filter(m -> m.isAnnotationPresent(Path.class)). // TODO switch to passing vxmsShared
                forEach(restMethod -> initRestMethod(vxmsShared, router, service, restMethod));
    }

    /**
     * Initialize a specific REST method from Service
     *
     * @param vxmsShared         the vxmsShared instance, containing the Vertx instance and other shared objects per instance
     * @param router     The Router object
     * @param service    The Service itself
     * @param restMethod the REST Method
     */
    private static void initRestMethod(VxmsShared vxmsShared, Router router, Object service, Method restMethod) {
        final Path path = restMethod.getAnnotation(Path.class);
        final Stream<Method> errorMethodStream = getRESTMethods(service, path.value()).stream().filter(method -> method.isAnnotationPresent(OnRestError.class));
        final Optional<Consumes> consumes = Optional.ofNullable(restMethod.isAnnotationPresent(Consumes.class) ? restMethod.getAnnotation(Consumes.class) : null);
        final Optional<GET> get = Optional.ofNullable(restMethod.isAnnotationPresent(GET.class) ? restMethod.getAnnotation(GET.class) : null);
        final Optional<POST> post = Optional.ofNullable(restMethod.isAnnotationPresent(POST.class) ? restMethod.getAnnotation(POST.class) : null);
        final Optional<OPTIONS> options = Optional.ofNullable(restMethod.isAnnotationPresent(OPTIONS.class) ? restMethod.getAnnotation(OPTIONS.class) : null);
        final Optional<PUT> put = Optional.ofNullable(restMethod.isAnnotationPresent(PUT.class) ? restMethod.getAnnotation(PUT.class) : null);
        final Optional<DELETE> delete = Optional.ofNullable(restMethod.isAnnotationPresent(DELETE.class) ? restMethod.getAnnotation(DELETE.class) : null);

        get.ifPresent(g -> initHttpGet(vxmsShared, router, service, restMethod, path, errorMethodStream, consumes));
        post.ifPresent(g -> initHttpPost(vxmsShared, router, service, restMethod, path, errorMethodStream, consumes));
        options.ifPresent(g -> initHttpOptions(vxmsShared, router, service, restMethod, path, errorMethodStream, consumes));
        put.ifPresent(g -> initHttpPut(vxmsShared, router, service, restMethod, path, errorMethodStream, consumes));
        delete.ifPresent(g -> initHttpDelete(vxmsShared, router, service, restMethod, path, errorMethodStream, consumes));

        if (!get.isPresent() && !post.isPresent() && !options.isPresent() && !put.isPresent() && !delete.isPresent()) {
            initHttpAll(vxmsShared, router, service, restMethod, path, errorMethodStream, consumes);
        }
    }

    private static void initHttpOperation(String methodId, VxmsShared vxmsShared, Object service, Method restMethod, Route route, Stream<Method> errorMethodStream, Optional<Consumes> consumes, Class<? extends Annotation> httpAnnotation) {
        final Optional<Method> errorMethod = errorMethodStream.filter(method -> method.isAnnotationPresent(httpAnnotation)).findFirst();
        initHttpRoute(methodId, vxmsShared, service, restMethod, consumes, errorMethod, route);
    }

    private static void initHttpAll(VxmsShared vxmsShared, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Optional<Method> errorMethod = errorMethodStream.findFirst();
        final Route route = router.route(URIUtil.cleanPath(path.value()));
        final Vertx vertx = vxmsShared.getVertx();
        final Context context = vertx.getOrCreateContext();
        final String methodId = path.value() + HTTP_ALL + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpRoute(methodId, vxmsShared, service, restMethod, consumes, errorMethod, route);
    }

    private static void initHttpDelete(VxmsShared vxmsShared, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.delete(URIUtil.cleanPath(path.value()));
        final Vertx vertx = vxmsShared.getVertx();
        final Context context = vertx.getOrCreateContext();
        final String methodId = path.value() + DELETE.class.getName() + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, service, restMethod, route, errorMethodStream, consumes, DELETE.class);
    }

    private static void initHttpPut(VxmsShared vxmsShared, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.put(URIUtil.cleanPath(path.value()));
        final Vertx vertx = vxmsShared.getVertx();
        final Context context = vertx.getOrCreateContext();
        final String methodId = path.value() + PUT.class.getName() + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, service, restMethod, route, errorMethodStream, consumes, PUT.class);
    }

    private static void initHttpOptions(VxmsShared vxmsShared, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.options(URIUtil.cleanPath(path.value()));
        final Vertx vertx = vxmsShared.getVertx();
        final Context context = vertx.getOrCreateContext();
        final String methodId = path.value() + OPTIONS.class.getName() + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, service, restMethod, route, errorMethodStream, consumes, OPTIONS.class);
    }

    private static void initHttpPost(VxmsShared vxmsShared, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.post(URIUtil.cleanPath(path.value()));
        final Vertx vertx = vxmsShared.getVertx();
        final Context context = vertx.getOrCreateContext();
        final String methodId = path.value() + POST.class.getName() + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, service, restMethod, route, errorMethodStream, consumes, POST.class);
    }

    protected static void initHttpGet(VxmsShared vxmsShared, Router router, Object service, Method restMethod, Path path, Stream<Method> errorMethodStream, Optional<Consumes> consumes) {
        final Route route = router.get(URIUtil.cleanPath(path.value()));
        final Vertx vertx = vxmsShared.getVertx();
        final Context context = vertx.getOrCreateContext();
        final String methodId = path.value() + GET.class.getName() + ConfigurationUtil.getCircuitBreakerIDPostfix(context.config());
        initHttpOperation(methodId, vxmsShared, service, restMethod, route, errorMethodStream, consumes, GET.class);
    }

    private static void initHttpRoute(String methodId, VxmsShared vxmsShared, Object service, Method restMethod, Optional<Consumes> consumes, Optional<Method> errorMethod, Route route) {
        route.handler(routingContext ->
                handleRESTRoutingContext(methodId, vxmsShared, service, restMethod, errorMethod, routingContext));
        updateHttpConsumes(consumes, route);
    }

    private static void updateHttpConsumes(Optional<Consumes> consumes, Route route) {
        consumes.ifPresent(cs -> {
            if (cs.value().length > 0) {
                Stream.of(cs.value()).forEach(route::consumes);
            }
        });
    }


    private static List<Method> getRESTMethods(Object service, String sName) {
        final String methodName = sName;
        final Method[] declaredMethods = service.getClass().getDeclaredMethods();
        return Stream.
                of(declaredMethods).
                filter(method -> filterRESTMethods(method, methodName)).
                collect(Collectors.toList());
    }

    private static boolean filterRESTMethods(final Method method, final String methodName) {
        return method.isAnnotationPresent(Path.class) && method.getAnnotation(Path.class).value().equalsIgnoreCase(methodName) ||
                method.isAnnotationPresent(OnRestError.class) && method.getAnnotation(OnRestError.class).value().equalsIgnoreCase(methodName);

    }

    private static void handleRESTRoutingContext(String methodId, VxmsShared vxmsShared, Object service, Method restMethod, Optional<Method> onErrorMethod, RoutingContext routingContext) {
        try {
            final Object[] parameters = getInvocationParameters(methodId, vxmsShared, service, restMethod, onErrorMethod, routingContext);
            ReflectionUtil.genericMethodInvocation(restMethod, () -> parameters, service);
        } catch (Throwable throwable) {
            handleRestError(methodId + "ERROR", vxmsShared, service, onErrorMethod, routingContext, throwable);
        }
    }

    private static Object[] getInvocationParameters(String methodId, VxmsShared vxmsShared, Object service, Method restMethod, Optional<Method> onErrorMethod, RoutingContext routingContext) {
        final Consumer<Throwable> throwableConsumer = throwable -> handleRestError(methodId + "ERROR", vxmsShared, service, onErrorMethod, routingContext, throwable);
        return ReflectionUtil.invokeRESTParameters(
                routingContext,
                restMethod,
                null,
                new RestHandler(methodId, routingContext, vxmsShared, null, throwableConsumer));
    }

    private static void handleRestError(String methodId, VxmsShared vxmsShared, Object service, Optional<Method> onErrorMethod, RoutingContext routingContext, Throwable throwable) {
        if (onErrorMethod.isPresent()) {
            invokeOnErrorMethod(methodId, vxmsShared, service, onErrorMethod, routingContext, throwable);
        } else {
            // TODO add SPI for custom failure handling
            failRequest(routingContext, throwable);
        }
    }


    private static void invokeOnErrorMethod(String methodId, VxmsShared vxmsShared, Object service, Optional<Method> onErrorMethod, RoutingContext routingContext, Throwable throwable) {
        onErrorMethod.ifPresent(errorMethod -> {
            try {
                ReflectionUtil.genericMethodInvocation(errorMethod, () -> ReflectionUtil.invokeRESTParameters(routingContext, errorMethod, throwable, new RestHandler(methodId, routingContext, vxmsShared, throwable, null)), service);
            } catch (Throwable t) {
                failRequest(routingContext, t);
            }

        });
    }

    private static void failRequest(RoutingContext routingContext, Throwable throwable) {
        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).setStatusMessage(throwable.getMessage()).end();
        throwable.printStackTrace();
    }


}
