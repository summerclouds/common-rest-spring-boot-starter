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

import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.summerclouds.common.core.error.IResult;
import org.summerclouds.common.core.error.RC;
import org.summerclouds.common.core.error.RC.Translated;
import org.summerclouds.common.core.log.Log;
import org.summerclouds.common.core.tool.MJson;
import org.summerclouds.common.core.tool.MSecurity;
import org.summerclouds.common.core.tool.MString;
import org.summerclouds.common.core.tool.MTracing;
import org.summerclouds.common.core.tracing.ISpan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

// https://www.baeldung.com/exception-handling-for-rest-with-spring

public class RestResponseStatusExceptionResolver extends AbstractHandlerExceptionResolver {

    private Log log = Log.getLog(RestResponseStatusExceptionResolver.class);

    @Override
    protected ModelAndView doResolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        try {
            ISpan span = MTracing.current();
            if (span != null) span.setError(ex);
        } catch (Throwable t) {
        }
        try {
            log.e("rest request {1} {2} failed", request.getMethod(), request.getPathInfo(), ex);
        } catch (Throwable t) {
        }
        try {
            ObjectNode out = MJson.createObjectNode();
            out.put("_timestamp", System.currentTimeMillis());
            out.put("_user", MString.valueOf(MSecurity.getCurrent()));
            out.put("_trace", MTracing.getTraceId());
            String msg = ex.getMessage();
            if (msg == null) msg = ex.getClass().getSimpleName();
            if (ex instanceof IResult) {
                IResult result = (IResult) ex;
                response.setStatus(result.getReturnCode());
                out.put("_error", result.getReturnCode());

                Locale locale = request.getLocale();
                Translated translated = RC.translate(locale, result.getMessage());
                out.put("_errorMessage", translated.getTranslated());
                if (translated.getArray() != null) out.set("_errorArray", translated.getArray());
                out.put("_errorString", translated.getOriginal());

            } else if (ex instanceof ResponseStatusException) {
                ResponseStatusException result = (ResponseStatusException) ex;
                response.setStatus(result.getRawStatusCode());
                out.put("_error", result.getRawStatusCode());
                out.put("_errorMessage", result.getLocalizedMessage());
                out.put("_errorString", result.getMessage());
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                out.put("_error", HttpStatus.BAD_REQUEST.value());
                out.put("_errorMessage", msg);
            }
            if (msg != null && msg.startsWith("[") && msg.endsWith("]")) {
                try {
                    JsonNode array = MJson.load(msg);
                    out.set("_errorArray", array);
                } catch (Throwable t) {
                }
            }
            ServletOutputStream os = response.getOutputStream();
            MJson.write(out, os);
            os.flush();
        } catch (Throwable t) {
            t.printStackTrace(); // XXX
        }

        return new ModelAndView();
    }
}
