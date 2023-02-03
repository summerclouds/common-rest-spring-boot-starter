/**
 * Copyright (C) 2022 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.summerclouds.common.restree.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.servlet.http.HttpServletRequest;

import org.summerclouds.common.core.cfg.CfgBoolean;
import org.summerclouds.common.core.log.MLog;
import org.summerclouds.common.core.util.WeakMapList;
import org.summerclouds.common.restree.CallContext;
import org.summerclouds.common.restree.RestRegistry;
import org.summerclouds.common.restree.RestSocket;
import org.summerclouds.common.restree.api.Node;
import org.summerclouds.common.restree.api.RestApi;
import org.summerclouds.common.restree.api.RestNodeService;

public abstract class AbstractRestApi extends MLog implements RestApi {

    protected RestRegistry register = new RestRegistry();
    protected WeakMapList<String, RestSocket> sockets = new WeakMapList<>();

    public static final CfgBoolean REQUIRE_SECURITY =
            new CfgBoolean(RestApi.class, "requireSecurity", false);

    @Override
    public Map<String, RestNodeService> getRestNodeRegistry() {
        return register.getRegistry();
    }

    @Override
    public Node lookup(List<String> parts, Class<? extends Node> lastNode, CallContext context)
            throws Exception {
        return register.lookup(parts, lastNode, context);
    }

    @Override
    public String getNodeId(Node node) {
        // return node instanceof RestNodeService ? ((RestNodeService)node).getNodeId() :
        // node.getClass().getCanonicalName();
        return node.getClass().getCanonicalName();
    }

    @Override
    public Node getNode(String ident) {
        //        String suffix = "-" + ident;
        //        for (Entry<String, RestNodeService> entry : register.getRegistry().entrySet())
        //            if (entry.getKey().endsWith(suffix)) return entry.getValue();
        for (RestNodeService entry : register.getRegistry().values())
            if (entry.getClass().getCanonicalName().equals(ident)) return entry;
        return null;
    }

    @Override
    public void unregister(RestSocket socket) {
        String nodeId = socket.getNodeId();
        synchronized (sockets) {
            sockets.removeEntry(nodeId, socket);
        }
    }

    @Override
    public void register(RestSocket socket) {
        String nodeId = socket.getNodeId();
        synchronized (sockets) {
            sockets.putEntry(nodeId, socket);
        }
    }

    @Override
    public void forEachSocket(Node node, Consumer<RestSocket> f) {
        String nodeId = getNodeId(node);
        List<RestSocket> list = null;
        synchronized (sockets) {
            list = sockets.getClone(nodeId);
        }
        list.forEach(
                v -> {
                    if (!v.isClosed()) f.accept(v);
                    //                    v.getSubject().execute(() -> f.accept(v) ); // not needed
                    // should be done by caller if recommended
                });
    }

    @Override
    public List<String> getSocketIds() {
        return new ArrayList<>(sockets.keySet());
    }

    @Override
    public int getSocketCount(String nodeId) {
        List<RestSocket> list = sockets.get(nodeId);
        if (list == null) return 0;
        return list.size();
    }

    @Override
    public String getRemoteAddress(Object request) {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            String remote = req.getRemoteAddr();
            remote = remoteAddressMapping(req, remote);
            return remote;
        }
        //        if (request instanceof RestWebSocket) {
        //            RestWebSocket req1 = (RestWebSocket)request;
        //            Object req2 = req1.getContext().getOriginalRequest();
        //            if (req2 != null && req2 instanceof HttpServletRequest) { // maybe not
        // correct, it's UpgradeRequest
        //                HttpServletRequest req3 = (HttpServletRequest)req2;
        //                String remote = req3.getRemoteAddr();
        //                remote = remoteAddressMapping(req3, remote);
        //                return remote;
        //            }
        //        }
        return null;
    }

    protected String remoteAddressMapping(HttpServletRequest request, String remote) {
        if (remote.equals("127.0.0.1")) {
            String forward = request.getHeader("X-Forwarded-For");
            if (forward != null) {
                int pos = forward.indexOf(',');
                if (pos > 0) forward = forward.substring(0, pos);
                return forward;
            }
        }
        return remote;
    }
}
