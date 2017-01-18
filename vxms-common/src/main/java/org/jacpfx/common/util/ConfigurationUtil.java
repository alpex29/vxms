package org.jacpfx.common.util;

import io.vertx.core.json.JsonObject;
import org.jacpfx.common.CustomServerOptions;
import org.jacpfx.common.DefaultServerOptions;
import org.jacpfx.common.ServiceEndpoint;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class ConfigurationUtil {


    /**
     * Returns a String configuration value by key or return default value
     * @param config, the configuration object
     * @param propertyName the property name to look for
     * @param defaultValue the fallback value
     * @return the String configuration value
     */
    public static String getStringConfiguration(final JsonObject config, String propertyName, String defaultValue) {
        String env = System.getenv(propertyName);
        if (env != null && !env.isEmpty()) return env;
        return config.getString(propertyName, defaultValue);
    }
    /**
     * Returns an Integer configuration value by key or return default value
     * @param config, the configuration object
     * @param propertyName the property name to look for
     * @param defaultValue the fallback value
     * @return the Integer configuration value
     */
    public static Integer getIntegerConfiguration(final JsonObject config, String propertyName, int defaultValue) {
        String env = System.getenv(propertyName);
        if (env != null && !env.isEmpty()) return Integer.valueOf(env);
        return config.getInteger(propertyName, defaultValue);
    }


    /**
     * Returns the service name defined in {@link ServiceEndpoint} annotation or passed by configuration
     * @param config, the configuration object
     * @param clazz, the service class containing the {@link ServiceEndpoint} annotation
     * @return
     */
    public static String getServiceName(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            final org.jacpfx.common.ServiceEndpoint name = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "service-name", name.name());
        }
        return getStringConfiguration(config, "service-name", clazz.getSimpleName());
    }


    public static Integer getEndpointPort(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint endpoint = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getIntegerConfiguration(config, "port", endpoint.port());
        }
        return getIntegerConfiguration(config, "port", 8080);
    }

    public static String getEndpointHost(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "host", selfHosted.host());
        }
        return getStringConfiguration(config, "host", getHostName());
    }


    public static String getContextRoot(final JsonObject config, Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            final org.jacpfx.common.ServiceEndpoint endpoint = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            return getStringConfiguration(config, "context-root", endpoint.contextRoot());
        }
        return getStringConfiguration(config, "context-root", "/");
    }

    /**
     * Returns the Method id's Postfix. This Id is used for the stateful circuit breaker as key in a shared map. The consequence is: If value is "unique" you get a random UUID, so for each method an unique shared state will be maintained.
     * If value is "local" the process PID will be returned, so all instances in one JVM will share on lock. In case of global, the postfix ist an empty String, so all instances (the same method signature) in the cluster will share this lock.
     * @param config
     * @return the correct POSTFIX for method id's
     */
    public static String getCircuitBreakerIDPostfix(final JsonObject config) {
        final String configValue = getStringConfiguration(config, "circuit-breaker-scope", "unique");
        switch (configValue){
            case "unique":
                return UUID.randomUUID().toString();
            case "global":
                return "";
            case "local":
                return getPID();
            default:
                return UUID.randomUUID().toString();
        }
    }

    /**
     * Returns the current PID of your JVM instance.
     * @return the PID number as a String
     */
    public static String getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        if (processName != null && processName.length() > 0) {
            try {
                return processName.split("@")[0];
            }
            catch (Exception e) {
                return "0";
            }
        }

        return "0";
    }

    /**
     * Returns the endpoint configuration object, defined in ServerEndoint annotation. If no definition is present a DefaultServerOptions instance will be created.
     * @param clazz
     * @return {@link CustomServerOptions} the Endpoint configuration
     */
    public static CustomServerOptions getEndpointOptions(Class clazz) {
        if (clazz.isAnnotationPresent(org.jacpfx.common.ServiceEndpoint.class)) {
            org.jacpfx.common.ServiceEndpoint selfHosted = (ServiceEndpoint) clazz.getAnnotation(ServiceEndpoint.class);
            try {
                return selfHosted.options().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return new DefaultServerOptions();
    }

    public static String getHostName() {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostName;
    }
}
