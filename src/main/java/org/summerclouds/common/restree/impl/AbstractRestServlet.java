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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.summerclouds.common.core.cfg.CfgInt;
import org.summerclouds.common.core.cfg.CfgString;
import org.summerclouds.common.core.error.AccessDeniedException;
import org.summerclouds.common.core.error.MException;
import org.summerclouds.common.core.error.MRuntimeException;
import org.summerclouds.common.core.error.RC;
import org.summerclouds.common.core.log.Log;
import org.summerclouds.common.core.node.INode;
import org.summerclouds.common.core.node.IReadProperties;
import org.summerclouds.common.core.security.ISubject;
import org.summerclouds.common.core.tool.MHttp;
import org.summerclouds.common.core.tool.MJson;
import org.summerclouds.common.core.tool.MSecurity;
import org.summerclouds.common.core.tool.MString;
import org.summerclouds.common.core.tool.MTracing;
import org.summerclouds.common.core.util.Provider;
import org.summerclouds.common.restree.CallContext;
import org.summerclouds.common.restree.api.Node;
import org.summerclouds.common.restree.api.RestApi;
import org.summerclouds.common.restree.api.RestException;
import org.summerclouds.common.restree.api.RestResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Activate: blue-create de.mhus.rest.osgi.RestServlet
 * Test: http://localhost:8182/rest/public/?_action=ping&_method=POST
 */
public abstract class AbstractRestServlet extends HttpServlet {

    private static final String RESULT_TYPE_JSON = "json";
    private static final String RESULT_TYPE_HTTP = "http";

    private static final String PUBLIC_PATH_START = "/public/";
    private static final String PUBLIC_PATH = "/public";

    protected Log log = Log.getLog(this);

    private static final long serialVersionUID = 1L;

    private CfgString CFG_CORS_ORIGIN = new CfgString(getClass(), "corsOrigin", "*");
    private CfgString CFG_CORS_HEADERS = new CfgString(getClass(), "corsHeaders", "*");
    private CfgInt CFG_URL_REMOVE_PARTS = new CfgInt(getClass(), "urlRemoveParts", 2);

    public abstract RestApi getRestService();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // System.out.println(">>> " + req.getPathInfo());
        response.setHeader("Access-Control-Allow-Origin", CFG_CORS_ORIGIN.value());
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, HEAD, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", CFG_CORS_HEADERS.value());
        response.setHeader("Access-Control-Max-Age", "0");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Vary", "*");

        response.setCharacterEncoding(MString.CHARSET_UTF_8); // default

        final RestApi restService = getRestService();
        if (!restService.checkSecurityRequest(request, response)) {
            log.d("request blocked by security");
            return;
        }

        // subject
        ISubject subject = MSecurity.getCurrent();
        //		IScope scope = null;
        try {

            final String path = request.getPathInfo();

            if (path == null || path.length() < 1) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // method
            String method = request.getParameter("_method");
            if (method == null) method = request.getMethod();
            method = method.toUpperCase();

            if (method.equals(MHttp.METHOD_OPTIONS)) {
                // nothing more to do
                return;
            }

            MSecurity.get().touch();

            // parts of path
            List<String> parts = new LinkedList<String>(Arrays.asList(path.split("/")));
            if (parts.size() < CFG_URL_REMOVE_PARTS.value()) {
                response.sendError(RC.NOT_FOUND);
                return;
            }
            for (int i = 0; i < CFG_URL_REMOVE_PARTS.value(); i++)
                parts.remove(0); // [empty], 'rest'

            Map<String, String[]> parameters = request.getParameterMap();
            if (method.equals(MHttp.METHOD_POST)
                    || method.equals(MHttp.METHOD_PUT)
                    || method.equals(MHttp.METHOD_DELETE)) {
                try {
                    String payload = getBody(request);
                    if (MString.isSet(payload)) {
                        INode node = INode.readNodeFromString(payload);
                        // XXX
                    }
                } catch (Exception t) {
                    log.d(t);
                }
            }

            // create call context object
            CallContext callContext =
                    new CallContext(
                            request,
                            response,
                            new CachedRestRequest(
                                    parameters,
                                    null,
                                    new Provider<InputStream>() {

                                        @Override
                                        public InputStream get() {
                                            try {
                                                return request.getInputStream();
                                            } catch (IOException e) {
                                                log.d(e);
                                                return null;
                                            }
                                        }
                                    }),
                            MHttp.toMethod(method));

            RestResult res = null;

            if (method.equals(MHttp.METHOD_HEAD)) {
                // nothing more to do
                return;
            }

            if (!restService.checkSecurityPrepared(callContext)) {
                log.d("request blocked by security", path);
                return;
            }

            Node item = restService.lookup(parts, null, callContext);

            if (item == null) {
                sendError(
                        request,
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Resource Not Found",
                        null,
                        null,
                        subject);
                return;
            }

            // log access
            logAccess(
                    getRestService().getRemoteAddress(request),
                    request.getRemotePort(),
                    subject,
                    method,
                    request.getPathInfo(),
                    request.getParameterMap());

            if (method.equals(MHttp.METHOD_GET)) {
                restService.checkPermission(item, "read", callContext);
                res = item.doRead(callContext);
            } else if (method.equals(MHttp.METHOD_POST)) {

                if (callContext.hasAction()) {
                    restService.checkPermission(item, callContext.getAction(), callContext);
                    res = item.doAction(callContext);
                } else {
                    restService.checkPermission(item, "create", callContext);
                    res = item.doCreate(callContext);
                }
            } else if (method.equals(MHttp.METHOD_PUT)) {
                restService.checkPermission(item, "update", callContext);
                res = item.doUpdate(callContext);
            } else if (method.equals(MHttp.METHOD_DELETE)) {
                restService.checkPermission(item, "delete", callContext);
                res = item.doDelete(callContext);
            } else if (method.equals(MHttp.METHOD_TRACE)) {

            }

            if (res == null) {
                sendError(
                        request,
                        response,
                        HttpServletResponse.SC_NOT_IMPLEMENTED,
                        null,
                        null,
                        null,
                        subject);
                return;
            }

            try {
                if (res != null) {
                    // resp.setHeader("Encapsulated", "result");
                    if (!restService.checkSecurityResult(callContext, res)) {
                        log.d("result blocked by security", res);
                        return;
                    }
                    log.d("result", res);
                    int rc = res.getReturnCode();
                    if (rc < 0) response.setStatus(-rc);
                    response.setContentType(res.getContentType(callContext));
                    res.write(callContext, response.getWriter());
                }
            } catch (Exception t) {
                log.d(t);
                sendError(
                        request,
                        response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        t.getMessage(),
                        t,
                        null,
                        subject);
                return;
            }

        } catch (AccessDeniedException e) {
            log.d(e);
            sendError(request, response, 404, e.getMessage(), e, null, subject);
            return;
        } catch (RestException t) {
            log.d(t);
            sendError(
                    request,
                    response,
                    t.getReturnCode(),
                    t.getMessage(),
                    t,
                    t.getParameters(),
                    subject);
            return;
        } catch (MException t) {
            log.d(t);
            sendError(request, response, t.getReturnCode(), t.getMessage(), t, null, subject);
        } catch (MRuntimeException t) {
            log.d(t);
            sendError(request, response, t.getReturnCode(), t.getMessage(), t, null, subject);
        } catch (Exception t) {
            log.d(t);
            sendError(
                    request,
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    t.getMessage(),
                    t,
                    null,
                    subject);
        }
    }

    public static String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }

    public boolean isPublicPath(String path) {
        return path.startsWith(PUBLIC_PATH_START) || path.equals(PUBLIC_PATH);
    }

    private void logAccess(
            String remoteAddr,
            int remotePort,
            ISubject subject,
            String method,
            String pathInfo,
            @SuppressWarnings("rawtypes") Map parameterMap) {

        String paramLog = getParameterLog(parameterMap);
        log.d(
                "restaccess",
                (subject == null ? "?" : subject.getPrincipal()),
                MTracing.get().getTraceId(),
                method,
                pathInfo,
                "\n Remote: "
                        + remoteAddr
                        + ":"
                        + remotePort
                        + "\n Subject: "
                        + (subject == null ? "?" : subject.getPrincipal())
                        + "\n Method: "
                        + method
                        + "\n Request: "
                        + pathInfo
                        + "\n Parameters: "
                        + paramLog
                        + "\n");
    }

    private String getParameterLog(Map<?, ?> parameterMap) {
        StringBuilder out = new StringBuilder().append('{');
        for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
            out.append('\n').append(entry.getKey()).append("=[");
            Object val = entry.getValue();
            if (val == null) {
            } else if (val.getClass().isArray()) {
                boolean first = true;
                Object[] arr = (Object[]) val;
                for (Object o : arr) {
                    if (first) first = false;
                    else out.append(',');
                    out.append(o);
                }
            } else {
                out.append(val);
            }
            out.append("] ");
        }
        out.append('}');
        return out.toString();
    }

    private void sendError(
            HttpServletRequest req,
            HttpServletResponse resp,
            int errNr,
            String errMsg,
            Throwable t,
            IReadProperties parameters,
            ISubject user)
            throws IOException {

        log.d("error", errNr, errMsg, t);

        if (errMsg == null && t != null) errMsg = t.getMessage();
        if (errMsg == null && t != null) errMsg = t.getClass().getSimpleName();

        // error result type
        String errorResultType = req.getParameter("_errorResult");
        if (errorResultType == null) errorResultType = RESULT_TYPE_JSON;

        if (errorResultType.equals(RESULT_TYPE_HTTP)) {
            resp.sendError(errNr, errMsg);
            return;
        }

        if (errorResultType.equals(RESULT_TYPE_JSON)) {

            if (!resp.isCommitted()) resp.setStatus(errNr);
            //			if (errNr == HttpServletResponse.SC_UNAUTHORIZED)
            //				resp.setStatus(errNr);
            //			else
            //				resp.setStatus(HttpServletResponse.SC_OK);

            PrintWriter w = resp.getWriter();
            ObjectMapper m = new ObjectMapper();

            ObjectNode json = m.createObjectNode();
            if (parameters != null)
                parameters.forEach(entry -> MJson.setValue(json, entry.getKey(), entry.getValue()));
            json.put("_timestamp", System.currentTimeMillis());
            if (user != null) json.put("_user", String.valueOf(user.getPrincipal()));
            json.put("_error", errNr);
            json.put("_trace", MTracing.getTraceId());
            json.put("_errorMessage", errMsg);
            if (errMsg != null && errMsg.startsWith("[") && errMsg.endsWith("]")) {
                try {
                    JsonNode errArray = MJson.load(errMsg);
                    json.set("_errorArray", errArray);
                } catch (Exception t2) {
                }
            }

            resp.setContentType("application/json");
            m.writeValue(w, json);

            return;
        }
    }
}
