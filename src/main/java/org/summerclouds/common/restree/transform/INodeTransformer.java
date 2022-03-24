/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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
package org.summerclouds.common.restree.transform;

import org.summerclouds.common.core.node.INode;
import org.summerclouds.common.core.node.JsonNodeBuilder;

import com.fasterxml.jackson.databind.JsonNode;

public class INodeTransformer implements ObjectTransformer {

    @Override
    public JsonNode toJsonNode(Object obj) {
        if (obj instanceof INode) {
            JsonNodeBuilder builder = new JsonNodeBuilder();
            JsonNode out = builder.writeToJsonNode((INode) obj);
            return out;
        }
        return null;
    }
}
