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

import java.io.IOException;

import org.summerclouds.common.core.log.MLog;
import org.summerclouds.common.core.pojo.MPojo;
import org.summerclouds.common.core.pojo.PojoModelFactory;
import org.summerclouds.common.core.tool.MJson;
import org.summerclouds.common.restree.util.RestUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PojoTransformer extends MLog implements ObjectTransformer {

    @Override
    public JsonNode toJsonNode(Object obj) {

        PojoModelFactory schema = getPojoModelFactory();
        ObjectNode jRoot = MJson.createObjectNode();
        try {
            MPojo.pojoToJson(obj, jRoot, schema, true);
        } catch (IOException e) {
            log().e("to json failed", obj, e);
        }
        return jRoot;
    }

    protected PojoModelFactory getPojoModelFactory() {
        return RestUtil.getPojoModelFactory();
    }
}
