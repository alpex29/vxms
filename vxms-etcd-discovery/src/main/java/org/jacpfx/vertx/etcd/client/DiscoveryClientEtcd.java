package org.jacpfx.vertx.etcd.client;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import org.jacpfx.vertx.registry.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 29.05.16.
 */
public class DiscoveryClientEtcd implements DiscoveryClient {
    private static final String CACHE_KEY = "local";
    private static final String MAP_KEY = "cache";
    private final HttpClientOptions options;
    private final SharedData data;
    private final Vertx vertx;
    private final String domainname;
    private final URI fetchAll;
    private final String discoveryServerHost;
    private final int discoveryServerPort;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;


    public DiscoveryClientEtcd(Vertx vertx, HttpClientOptions options, String domainname, URI fetchAll, String discoveryServerHost, int discoveryServerPort) {
        this.vertx = vertx;
        this.data = vertx.sharedData();
        this.domainname = domainname;
        this.fetchAll = fetchAll;
        this.discoveryServerHost = discoveryServerHost;
        this.discoveryServerPort = discoveryServerPort;
        this.options = options;
    }


    private  HttpClientOptions getOptions() {
        return (discoveryServerHost != null && discoveryServerPort > 0) ? options
                .setDefaultHost(discoveryServerHost)
                .setDefaultPort(discoveryServerPort) : options;
    }

    public DiscoveryClientEtcd(Vertx vertx, HttpClientOptions options, String domainname, URI fetchAll) {
        this(vertx, options, domainname, fetchAll, null, 0);

    }

    /**
     * find service by name
     *
     * @param serviceName, the name of the service to find
     * @return DCServiceName the builder to execute the search process
     */
    @Override
    public OnSuccessDiscovery find(String serviceName) {
        return new OnSuccessDiscovery(vertx, this, serviceName);
    }


    @Override
    public void findNode(String serviceName, Consumer<NodeResponse> consumer) {
        final String service = serviceName.startsWith("/") ? serviceName : "/" + serviceName;
        retrieveKeysFromCache(root -> {
            final String key = "/" + domainname + service;
            final Node serviceNode = findNode(root.getNode(), key);
            if (serviceNode.getNodes() != null & serviceNode.getKey().equals(key)) {
                final Optional<Node> first = serviceNode.getNodes().stream().findAny();
                if (!first.isPresent()) {
                    this.findNodeFromEtcd(service, consumer);
                }
                first.ifPresent(node -> handleServiceNode(service, consumer, node, serviceNode));
            } else {
                findNodeFromEtcd(service, consumer);
            }
        }, () -> findNodeFromEtcd(service, consumer));
    }


    /**
     * @param serviceName the service name to find
     * @param consumer    the consumer to execute
     * @param node        the first node found
     * @param serviceNode the parent node
     */
    private void handleServiceNode(String serviceName, Consumer<NodeResponse> consumer, Node node, Node serviceNode) {
        LocalDateTime dateTime = LocalDateTime.parse(node.getExpiration(), formatter);
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);
        // check if expire date is still valid
        if (dateTime.compareTo(nowUTC.toLocalDateTime()) >= 0) {
            consumer.accept(new NodeResponse(serviceNode.getNodes(), domainname, true, null));
        } else {
            findNodeFromEtcd(serviceName, consumer);
        }
    }

    private void findNodeFromEtcd(String serviceName, Consumer<NodeResponse> consumer) {
        final String service = serviceName.startsWith("/") ? serviceName : "/" + serviceName;
        retrieveKeys(root -> {
            putRootToCache(root);
            final String key = "/" + domainname + service;
            final Node serviceNode = findNode(root.getNode(), key);
            if (serviceNode.getNodes() != null && serviceNode.getKey().equals(key)) {
                boolean isEmpty = serviceNode.getNodes().isEmpty();
                consumer.accept(new NodeResponse(serviceNode.getNodes(), domainname, !isEmpty, isEmpty ? new NodeNotFoundException("no active node found") : null));
            } else {
                consumer.accept(new NodeResponse(Collections.emptyList(), domainname, false, new NodeNotFoundException("service not found")));
            }
        });
    }


    @Override
    public void isConnected(Consumer<Future<?>> connected) {
        try {
            vertx.createHttpClient(getOptions()).get(fetchAll.toString()).
                    exceptionHandler(ex -> connected.accept(Future.failedFuture(ex))).
                    handler(handler -> connected.accept(Future.succeededFuture(true))).end();
        } catch (Exception s) {
            connected.accept(Future.failedFuture(s));
        }

    }

    private void retrieveKeys(Consumer<Root> consumer) {
        vertx.createHttpClient(getOptions()).getAbs(fetchAll.toString(), handler -> handler.
                exceptionHandler(error -> consumer.accept(new Root())).
                bodyHandler(body -> consumer.accept(decodeRoot(body)))
        ).end();
    }

    private Root decodeRoot(Buffer body) {
        try {
            return Json.decodeValue(new String(body.getBytes()), Root.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Root();
    }

    private void retrieveKeysFromCache(Consumer<Root> consumer, Runnable onMiss) {
        final LocalMap<String, Root> cache = data.getLocalMap(MAP_KEY);
        final Root local = cache.get(CACHE_KEY);
        if (local != null) {
            consumer.accept(local);
        } else {
            onMiss.run();
        }
    }

    private Node findNode(Node node, String key) {
        if (node == null) return Node.emptyNode();
        if (node.getKey() != null && node.getKey().equals(key)) return node;
        if (node.isDir() && node.getNodes() != null) return node.getNodes().stream().filter(n1 -> {
            final Node n2 = n1.isDir() ? findNode(n1, key) : n1;
            return n2.getKey().equals(key);
        }).findFirst().orElse(Node.emptyNode());
        return Node.emptyNode();
    }

    private void putRootToCache(Root root) {
        final LocalMap<String, Root> cache = data.getLocalMap(MAP_KEY);
        cache.put(CACHE_KEY, root);
    }
}
