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

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.summerclouds.common.core.log.MLog;

@org.springframework.web.bind.annotation.RestController
@ConditionalOnProperty(name = "org.summerclouds.restree.enabled", havingValue = "true")
public class RestController extends MLog {

    private RestServlet restServlet;

    @PostConstruct
    public void setup() {
        restServlet = new RestServlet();
        try {
            restServlet.init();
        } catch (ServletException e) {
            log().e(e);
        }
    }

    @RequestMapping("/rest/*")
    public void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            restServlet.service(request, response);
        } catch (Exception e) {
            log().e("Server Error occurred", e);
            throw new ServletException(e);
        }
    }
}
