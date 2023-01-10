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
package org.summerclouds.common.restree.result;

import java.io.PrintWriter;

import org.summerclouds.common.core.pojo.MPojo;
import org.summerclouds.common.core.pojo.PojoModelFactory;
import org.summerclouds.common.core.tool.MHttp;
import org.summerclouds.common.core.tool.MString;
import org.summerclouds.common.restree.CallContext;
import org.summerclouds.common.restree.api.RestResult;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PojoResult implements RestResult {

    private String contentType;
    private Object obj;
    private int returnCode = 0;

    public PojoResult(Object obj, String contentType) {
        if (MString.isEmpty(contentType)) contentType = MHttp.CONTENT_TYPE_JSON;
        this.contentType = contentType;
        this.obj = obj;
    }

    @Override
    public void write(CallContext context, PrintWriter writer) throws Exception {
        if (obj == null) return;
        JsonResult json = new JsonResult();
        PojoModelFactory factory = MPojo.getDefaultModelFactory();
        if (obj.getClass().isArray()) {
            if (obj.getClass().getComponentType().isPrimitive()) {
                ArrayNode jArray = json.createArrayNode();
                for (Object aObj : (Object[]) obj) {
                    if (aObj == null) continue; // should not happen
                    MPojo.addJsonValue(jArray, aObj, factory, false, true, false);
                }
            } else {
                ArrayNode jArray = json.createArrayNode();
                for (Object aObj : (Object[]) obj) {
                    ObjectNode jObj = jArray.objectNode();
                    if (aObj != null) MPojo.pojoToJson(aObj, jObj, factory);
                    jArray.add(jObj);
                }
            }
        } else if (obj.getClass().isPrimitive()) {
            ObjectNode jObj = json.createObjectNode();
            MPojo.setJsonValue(jObj, "value", obj, factory, false, true, false);
        } else {
            ObjectNode jObj = json.createObjectNode();
            MPojo.pojoToJson(obj, jObj, factory);
        }
        json.write(context, writer);
    }

    @Override
    public String getContentType(CallContext context) {
        return contentType;
    }

    @Override
    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
}
