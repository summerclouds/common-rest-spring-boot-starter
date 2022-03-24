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
package org.summerclouds.common.restree.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.summerclouds.common.core.error.AccessDeniedException;
import org.summerclouds.common.core.error.MException;
import org.summerclouds.common.core.error.MRuntimeException;
import org.summerclouds.common.core.log.Log;
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

	public abstract RestApi getRestService();

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// System.out.println(">>> " + req.getPathInfo());
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, HEAD, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "*");
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

//			// tracing
//			SpanContext parentSpanCtx = null;
//			if (CFG_TRACE_FOLLOW.value()) {
//				parentSpanCtx = MTracing.get().tracer().extract(Format.Builtin.HTTP_HEADERS,
//						new TraceExtractRest(request));
//			}
//			String trace = request.getParameter("_trace");
//			if (MString.isEmpty(trace))
//				trace = CFG_TRACE_ACTIVE.value();
//
//			if (parentSpanCtx == null) {
//				scope = MTracing.start("rest", trace);
//			} else if (parentSpanCtx != null) {
//				Span span = ITracer.get().tracer().buildSpan("rest").asChildOf(parentSpanCtx).start();
//				scope = ITracer.get().activate(span);
//			}
//
//			if (MString.isSet(trace))
//				ITracer.get().activate(trace);
//
//			if (scope != null) {
//				// method
//				String method = request.getParameter("_method");
//				if (method == null)
//					method = request.getMethod();
//				method = method.toUpperCase();
//				Span span = ITracer.get().current();
//				Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_SERVER);
//				Tags.HTTP_METHOD.set(span, method);
//				Tags.HTTP_URL.set(span, request.getRequestURL().toString());
//				span.setTag("http.remote", getRestService().getRemoteAddress(request));
//				String pi = request.getPathInfo();
//				if (CFG_TRACE_PATH.value()) {
//					if (pi != null) {
//						int i = 0;
//						for (String part : pi.split("/")) {
//							span.setTag("urlpart" + i, part);
//							i++;
//						}
//					}
//				}
//				if (CFG_TRACE_PARAM.value()) {
//					Map<String, String[]> map = request.getParameterMap();
//					if (map != null) {
//						for (Map.Entry<String, String[]> me : map.entrySet())
//							span.setTag("param_" + me.getKey(), arrayToString(me.getValue()));
//					}
//				}
//				if (CFG_HEADER_TAGS.value()) {
//					Enumeration<String> enu = request.getHeaderNames();
//					while (enu.hasMoreElements()) {
//						String name = enu.nextElement();
//						StringBuilder sb = null;
//						if ("Authorization".equals(name)) {
//							sb = new StringBuilder();
//							String v = MString.beforeIndex(request.getHeader(name), ' ');
//							sb.append(v);
//							sb.append(" ***");
//						} else {
//							Enumeration<String> enu2 = request.getHeaders(name);
//							while (enu2.hasMoreElements()) {
//								String value = enu2.nextElement();
//								if (sb == null)
//									sb = new StringBuilder();
//								else
//									sb.append(",");
//								sb.append(value);
//							}
//						}
//						if (sb != null)
//							span.setTag("header_" + name, sb.toString());
//					}
//				}
//			}

			// method
			String method = request.getParameter("_method");
			if (method == null)
				method = request.getMethod();
			method = method.toUpperCase();

			if (method.equals(MHttp.METHOD_OPTIONS)) {
				// nothing more to do
				return;
			}

			MSecurity.get().touch();

			// parts of path
			List<String> parts = new LinkedList<String>(Arrays.asList(path.split("/")));
			if (parts.size() == 0)
				return; // XXX
			parts.remove(0); // [empty]
			parts.remove(0); // rest

			Map<String, String[]> parameters = request.getParameterMap();
			// check for payload and overlay parameters
			// TODO implement payload
			// String body = req.getReader().lines()
			// .reduce("", (accumulator, actual) -> accumulator + actual);

			// create call context object
			CallContext callContext = new CallContext(request, response,
					new CachedRestRequest(parameters, null, new Provider<InputStream>() {

						@Override
						public InputStream get() {
							try {
								return request.getInputStream();
							} catch (IOException e) {
								log.d(e);
								return null;
							}
						}
					}), MHttp.toMethod(method));

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
				sendError(request, response, HttpServletResponse.SC_NOT_FOUND, "Resource Not Found", null, null,
						subject);
				return;
			}

			// log access
			logAccess(getRestService().getRemoteAddress(request), request.getRemotePort(), subject, method,
					request.getPathInfo(), request.getParameterMap());

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
				sendError(request, response, HttpServletResponse.SC_NOT_IMPLEMENTED, null, null, null, subject);
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
					if (rc < 0)
						response.setStatus(-rc);
					response.setContentType(res.getContentType(callContext));
					res.write(callContext, response.getWriter());
				}
			} catch (Throwable t) {
				log.d(t);
				sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), t, null,
						subject);
				return;
			}

		} catch (AccessDeniedException e) {
			log.d(e);
			sendError(request, response, 404, e.getMessage(), e, null, subject);
			return;
		} catch (RestException t) {
			log.d(t);
			sendError(request, response, t.getReturnCode(), t.getMessage(), t, t.getParameters(), subject);
			return;
		} catch (MException t) {
			log.d(t);
			sendError(request, response, t.getReturnCode(), t.getMessage(), t, null, subject);
		} catch (MRuntimeException t) {
			log.d(t);
			sendError(request, response, t.getReturnCode(), t.getMessage(), t, null, subject);
		} catch (Throwable t) {
			log.d(t);
			sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), t, null,
					subject);
		}
	}

//	private String arrayToString(String[] value) {
//		if (value == null)
//			return "null";
//		if (value.length == 0)
//			return "";
//		if (value.length == 1)
//			return value[0];
//		return Arrays.toString(value);
//	}

	public boolean isPublicPath(String path) {
		return path.startsWith(PUBLIC_PATH_START) || path.equals(PUBLIC_PATH);
	}

	private void logAccess(String remoteAddr, int remotePort, ISubject subject, String method, String pathInfo,
			@SuppressWarnings("rawtypes") Map parameterMap) {

		String paramLog = getParameterLog(parameterMap);
		log.d("restaccess", (subject == null ? "?" : subject.getPrincipal()), MTracing.get().getTraceId(), method,
				pathInfo,
				"\n Remote: " + remoteAddr + ":" + remotePort + "\n Subject: "
						+ (subject == null ? "?" : subject.getPrincipal()) + "\n Method: " + method + "\n Request: "
						+ pathInfo + "\n Parameters: " + paramLog + "\n");
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
					if (first)
						first = false;
					else
						out.append(',');
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

	private void sendError(HttpServletRequest req, HttpServletResponse resp, int errNr, String errMsg,
			Throwable t, IReadProperties parameters, ISubject user) throws IOException {

		log.d("error", errNr, errMsg, t);

        if (errMsg == null && t != null) errMsg = t.getMessage();
        if (errMsg == null && t != null) errMsg = t.getClass().getSimpleName();

		// error result type
		String errorResultType = req.getParameter("_errorResult");
		if (errorResultType == null)
			errorResultType = RESULT_TYPE_JSON;

		if (errorResultType.equals(RESULT_TYPE_HTTP)) {
			resp.sendError(errNr, errMsg);
			return;
		}

		if (errorResultType.equals(RESULT_TYPE_JSON)) {

			if (!resp.isCommitted())
				resp.setStatus(errNr);
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
			if (user != null)
				json.put("_user", String.valueOf(user.getPrincipal()));
			json.put("_error", errNr);
			json.put("_trace", MTracing.getTraceId());
			json.put("_errorMessage", errMsg);
            if (errMsg != null && errMsg.startsWith("[") && errMsg.endsWith("]")) {
                try {
                    JsonNode errArray = MJson.load(errMsg);
                    json.set("_errorArray", errArray);
                } catch (Throwable t2) {}
            }
			
//			if (CFG_TRACE_RETURN.value() && ITracer.get().current() != null)
//				try {
//					ITracer.get().tracer().inject(ITracer.get().current().context(), Format.Builtin.TEXT_MAP,
//							new TraceJsonMap(json, "_"));
//				} catch (Throwable t2) {
//					log.d(t2);
//				}
			resp.setContentType("application/json");
			m.writeValue(w, json);

			return;
		}
	}

}
