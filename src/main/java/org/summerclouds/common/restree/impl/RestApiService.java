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

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

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
        for (RestNodeService service : map.values()) {
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
    public void onSummerApplicationStop() throws Exception {}
}
