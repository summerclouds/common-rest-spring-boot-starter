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

import javax.servlet.annotation.WebServlet;

import org.summerclouds.common.core.cfg.BeanRef;
import org.summerclouds.common.restree.api.RestApi;

@WebServlet(
        name = "RESTREE",
        description = "REST Tree Servlet",
        urlPatterns = {"/rest"})
public class RestServlet extends AbstractRestServlet {

    private static final long serialVersionUID = 1L;

    private BeanRef<RestApi> restApi = new BeanRef<>(RestApi.class);

    public RestServlet() {
        log.i("Start RestServlet");
    }

    public RestApi getRestService() {
        return restApi.bean();
    }
}
