package org.summerclouds.common.restree.impl;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.summerclouds.common.core.cfg.BeanRef;
import org.summerclouds.common.core.lang.SummerApplicationLifecycle;
import org.summerclouds.common.core.tool.MSpring;
import org.summerclouds.common.restree.CallContext;
import org.summerclouds.common.restree.api.Node;
import org.summerclouds.common.restree.api.RestNodeService;
import org.summerclouds.common.restree.api.RestResult;
import org.summerclouds.common.restree.api.RestSecurityService;

public class RestApiService extends AbstractRestApi implements SummerApplicationLifecycle {

    public BeanRef<RestSecurityService> securityService = new BeanRef<>(RestSecurityService.class);

	public void setup() {
		log().i("Start RestApiService");
		reset();
	}
	
	@Override
	public void reset() {
		register.getRegistry().clear();
		Map<String, RestNodeService> map = MSpring.getBeansOfType(RestNodeService.class);
		for (RestNodeService  service : map.values()) {
            for (String x : service.getParentNodeCanonicalClassNames()) {
                if (x != null) {
                    if (x.length() > 0
                            && !x.contains(
                                    ".")) // print a warning - class name without dot should be
                        // a mistake
                        log().w(
                                        "Register RestNode with malformed parent name - should be a class",
                                        service.getClass(),
                                        service.getNodeName(),
                                        x);
                    String key = x + "-" + service.getNodeName();
                    log().i("register", key, service.getClass().getCanonicalName());
                    register.getRegistry().put(key, service);
                }
            }

		}
	}

	@Override
	public void checkPermission(Node item, String action, CallContext callContext) {
        register.checkPermission(callContext, action);
	}

	@Override
	public boolean checkSecurityPrepared(CallContext callContext) {
        RestSecurityService s = securityService.bean();
        if (s == null) {
            if (REQUIRE_SECURITY.value()) {
                log().d("deny access to rest - wait for security");
                callContext.setResponseStatus(503);
                return false;
            }
            return true;
        }
        return s.checkSecurityPrepared(callContext);
	}

	@Override
	public boolean checkSecurityRequest(Object request, Object response) {
        RestSecurityService s = securityService.bean();
        if (s == null) {
            if (REQUIRE_SECURITY.value()) {
                log().d("deny access to rest - wait for security");
                if (response instanceof HttpServletResponse)
                    try {
                        ((HttpServletResponse) response).sendError(503);
                    } catch (IOException e) {
                        log().d(e);
                    }
                return false;
            }
            return true;
        }
        return s.checkSecurityRequest(request, response);
	}

	@Override
	public boolean checkSecurityResult(CallContext callContext, RestResult result) {
        RestSecurityService s = securityService.bean();
        if (s == null) {
            if (REQUIRE_SECURITY.value()) {
                log().d("deny access to rest - wait for security");
                callContext.setResponseStatus(503);
                return false;
            }
            return true;
        }
        return s.checkSecurityResult(callContext, result);
	}

    @Override
    public void onSummerApplicationStart() throws Exception {
        setup();
    }

    @Override
    public void onSummerApplicationStop() throws Exception {

    }
}
