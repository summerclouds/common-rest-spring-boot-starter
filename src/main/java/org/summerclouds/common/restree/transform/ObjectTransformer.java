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
package org.summerclouds.common.restree.transform;

import org.summerclouds.common.core.log.Log;
import org.summerclouds.common.restree.annotation.RestTransformer;

import com.fasterxml.jackson.databind.JsonNode;

public interface ObjectTransformer {

    ObjectTransformer DEFAULT = new PojoTransformer();

    static ObjectTransformer create(Class<?> owner) {
        if (owner == null) return DEFAULT;
        RestTransformer anno = owner.getAnnotation(RestTransformer.class);
        if (anno == null) return DEFAULT;
        Class<?>[] value = anno.value();
        if (value == null || value.length == 0) return new TransformList(); // empty
        if (value.length == 1) {
            try {
                return (ObjectTransformer) value[0].getConstructor().newInstance();
            } catch (Throwable t) {
                Log.getLog(ObjectTransformer.class)
                        .f("create transformer failed", owner, value[0], t);
                return new TransformList(); // empty
            }
        } else {
            TransformList out = new TransformList();
            for (Class<?> v : value) {
                try {
                    ObjectTransformer t = (ObjectTransformer) v.getConstructor().newInstance();
                    out.add(t);
                } catch (Throwable t) {
                    Log.getLog(ObjectTransformer.class).f("create transformer failed", owner, v, t);
                }
            }
            return out;
        }
    }

    /**
     * Transform an object to a jsonNode or return null if it is not supported.
     *
     * @param obj
     * @return Node or null
     */
    JsonNode toJsonNode(Object obj);
}
