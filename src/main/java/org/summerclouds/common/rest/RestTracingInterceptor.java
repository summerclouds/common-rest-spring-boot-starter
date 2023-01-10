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
package org.summerclouds.common.rest;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.summerclouds.common.core.cfg.CfgBoolean;
import org.summerclouds.common.core.tool.MSecurity;
import org.summerclouds.common.core.tool.MString;
import org.summerclouds.common.core.tool.MTracing;
import org.summerclouds.common.core.tracing.ISpan;

public class RestTracingInterceptor implements HandlerInterceptor {

    //	@Value("${org.summerclouds.common.rest.tracePathParts:false}")
    private CfgBoolean tracePathParts =
            new CfgBoolean("org.summerclouds.common.rest.tracePathParts", false);
    //	@Value("${org.summerclouds.common.rest.traceParameters:false}")
    private CfgBoolean traceParameters =
            new CfgBoolean("org.summerclouds.common.rest.traceParameters", false);
    //	@Value("${org.summerclouds.common.rest.traceHeaders:false}")
    private CfgBoolean traceHeaders =
            new CfgBoolean("org.summerclouds.common.rest.traceHeaders", false);

    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // add result headers

        String traceId = MTracing.getTraceId();
        if (MString.isSet(traceId)) response.setHeader("X-Summer-TraceId", traceId);
        String subject = MString.valueOf(MSecurity.getCurrent());
        if (MString.isSet(subject)) response.setHeader("X-Summer-Subject", subject);
        // response.setHeader("X-Summer-Timestamp", String.valueOf(System.currentTimeMillis()) );

        // add tracing info
        ISpan span = MTracing.current();
        if (handler != null) {
            span.setTag("mvc.controller", String.valueOf(handler));
        }
        if (tracePathParts.value()) {
            String path =
                    request.getServletPath()
                            + (request.getPathInfo() == null ? "" : "/" + request.getPathInfo());
            String[] parts = path.split("/");
            for (int i = 1; i < parts.length; i++) span.setTag("request.part" + i, parts[i]);
        }
        if (traceParameters.value()) {
            Map<String, String[]> map = request.getParameterMap();
            if (map != null) {
                for (Map.Entry<String, String[]> me : map.entrySet())
                    span.setTag("request.param_" + me.getKey(), arrayToString(me.getValue()));
            }
        }
        if (traceHeaders.value()) {
            Enumeration<String> enu = request.getHeaderNames();
            while (enu.hasMoreElements()) {
                String name = enu.nextElement();
                StringBuilder sb = null;
                if ("Authorization".equalsIgnoreCase(name)) {
                    sb = new StringBuilder();
                    String v = MString.beforeIndex(request.getHeader(name), ' ');
                    sb.append(v);
                    sb.append(" ***");
                } else {
                    Enumeration<String> enu2 = request.getHeaders(name);
                    while (enu2.hasMoreElements()) {
                        String value = enu2.nextElement();
                        if (sb == null) sb = new StringBuilder();
                        else sb.append(",");
                        sb.append(value);
                    }
                }
                if (sb != null) span.setTag("request.header_" + name, sb.toString());
            }
        }
        return true;
    }

    private String arrayToString(String[] value) {
        if (value == null) return "null";
        if (value.length == 0) return "";
        if (value.length == 1) return value[0];
        return Arrays.toString(value);
    }
}
