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
package org.summerclouds.common.rest.internal;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.summerclouds.common.core.log.MLog;
import org.summerclouds.common.core.log.PlainLog;
import org.summerclouds.common.rest.RestResponseStatusExceptionResolver;
import org.summerclouds.common.restree.api.RestApi;
import org.summerclouds.common.restree.api.RestNodeService;
import org.summerclouds.common.restree.impl.RestApiService;
import org.summerclouds.common.restree.nodes.PublicRestNode;

public class SpringSummerCloudsRestAutoConfiguration extends MLog {

    @Autowired RequestMappingHandlerMapping requestMappingHandlerMapping;

    public SpringSummerCloudsRestAutoConfiguration() {
        PlainLog.i("Start SpringSummerCloudsRestAutoConfiguration");
    }

    @PostConstruct
    public void setup() {
        final StringBuilder sb = new StringBuilder();
        Map<RequestMappingInfo, HandlerMethod> map =
                requestMappingHandlerMapping.getHandlerMethods();
        map.forEach((key, value) -> sb.append(key).append("=").append(value).append("\n"));
        log().i("REST CONTROLLERS", sb.toString());
    }

    // not working
    //	@Bean
    //	@ConditionalOnProperty(name="org.summerclouds.restree.enabled",havingValue="true")
    //	public ServletRegistrationBean<RestServlet> restServlet() {
    //		ServletRegistrationBean<RestServlet> srb = new ServletRegistrationBean<>(new RestServlet(),
    // "/rest/*");
    //		srb.setLoadOnStartup(1);
    //		srb.setName("restree");
    //		srb.setEnabled(true);
    //		return srb;
    //	}

    @Bean
    @ConditionalOnProperty(name = "org.summerclouds.restree.enabled", havingValue = "true")
    public RestApi restApi() {
        return new RestApiService();
    }

    @Bean
    @ConditionalOnProperty(name = "org.summerclouds.restree.public.enabled", havingValue = "true")
    public RestNodeService restNodePublic() {
        return new PublicRestNode();
    }

    @Bean
    public HandlerExceptionResolver errorHandler() {
        return new RestResponseStatusExceptionResolver();
    }
}
