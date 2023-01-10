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
package org.summerclouds.common.restree.node;

import java.util.List;

import org.summerclouds.common.restree.CallContext;
import org.summerclouds.common.restree.api.Node;

public abstract class SingleNode<T> extends AbstractNode {

    public static final String ID = "_id";
    public static final String OBJECT = "_obj";
    public static final String SOURCE = "source";
    public static final String INTERNAL_PREFIX = "_";

    @Override
    public Node lookup(List<String> parts, CallContext callContext) throws Exception {

        T obj = getObject(callContext);

        if (obj == null) return null;

        callContext.put(getManagedClassName() + OBJECT, obj);

        if (parts.size() < 1) return this;

        return callContext.lookup(parts, getClass());
    }

    @SuppressWarnings("unchecked")
    protected T getObjectFromContext(CallContext callContext) {
        return (T) callContext.get(getManagedClassName() + OBJECT);
    }

    protected abstract T getObject(CallContext context) throws Exception;
}
