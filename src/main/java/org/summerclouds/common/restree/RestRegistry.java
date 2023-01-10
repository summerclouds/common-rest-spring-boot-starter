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
package org.summerclouds.common.restree;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.summerclouds.common.core.cfg.CfgBoolean;
import org.summerclouds.common.core.error.AccessDeniedException;
import org.summerclouds.common.core.log.MLog;
import org.summerclouds.common.core.tool.MSecurity;
import org.summerclouds.common.restree.api.Node;
import org.summerclouds.common.restree.api.RestApi;
import org.summerclouds.common.restree.api.RestNodeService;

public class RestRegistry extends MLog {

    public static final CfgBoolean RELAXED = new CfgBoolean(RestApi.class, "aaaRelaxed", true);

    private Map<String, RestNodeService> register = Collections.synchronizedMap(new HashMap<>());

    public Map<String, RestNodeService> getRegistry() {
        return register;
    }

    public Node lookup(List<String> parts, CallContext context) throws Exception {
        return lookup(parts, null, context);
    }

    public Node lookup(List<String> parts, Class<? extends Node> lastNode, CallContext context)
            throws Exception {
        if (parts.size() < 1) return null;
        String name = parts.get(0);
        parts.remove(0);
        String lastNodeId =
                lastNode == null ? RestNodeService.ROOT_PARENT : lastNode.getCanonicalName();
        String ident = lastNodeId + "-" + name;
        RestNodeService next = register.get(ident);
        if (next == null) return null;

        checkPermission(context, next, ident, "access");

        context.setNodeIdent(ident); // remember last ident
        return next.lookup(parts, context);
    }

    public void checkPermission(CallContext context, String action) {
        String ident = context.getNodeIdent();
        if (ident == null) {
            log().d("ident is null");
            throw new AccessDeniedException("access denied (3)");
        }
        RestNodeService next = register.get(ident);
        if (next == null) {
            log().d("node is null", ident);
            throw new AccessDeniedException("access denied (4)");
        }
        checkPermission(context, next, ident, action);
    }

    public void checkPermission(
            CallContext context, RestNodeService next, String ident, String action) {

        if (!MSecurity.hasPermission("de.mhus.rest.core.node", action, ident))
            throw new AccessDeniedException("access denied (1)");
    }
}
