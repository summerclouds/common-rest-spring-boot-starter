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
package org.summerclouds.common.restree.result;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.summerclouds.common.core.security.ISubject;
import org.summerclouds.common.core.tool.MSecurity;
import org.summerclouds.common.restree.CallContext;
import org.summerclouds.common.restree.api.RestResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonResult implements RestResult {

    // private static Log log = Log.getLog(JsonResult.class);
    private static int nextId = 0;
    private JsonNode json;
    private long id;
    private static ObjectMapper m = new ObjectMapper();
    private int returnCode = 0;

    public JsonResult() {
        id = newId();
    }

    @Override
    public void write(CallContext context, PrintWriter writer) throws Exception {

        // log.d("result",id,json);
        if (json == null) {
            createObjectNode();
        }
        if (json.isObject()) {
            ((ObjectNode) json).put("_timestamp", System.currentTimeMillis());
            ((ObjectNode) json).put("_sequence", id);

            ISubject subject = MSecurity.getCurrent();
            ((ObjectNode) json).put("_user", String.valueOf(subject.getPrincipal()));
        }

        m.writeValue(writer, json);
    }

    @Override
    public String getContentType(CallContext context) {
        return "application/json";
    }

    private static synchronized long newId() {
        return nextId++;
    }

    public JsonNode getJson() {
        return json;
    }

    public void setJson(JsonNode json) {
        this.json = json;
    }

    public ObjectNode createObjectNode() {
        json = m.createObjectNode();
        return (ObjectNode) json;
    }

    @Override
    public String toString() {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        try {
            write(null, p);
        } catch (Exception e) {
        }
        p.flush();
        return w.toString();
    }

    public ArrayNode createArrayNode() {
        json = m.createArrayNode();
        return (ArrayNode) json;
    }

    @Override
    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
}
