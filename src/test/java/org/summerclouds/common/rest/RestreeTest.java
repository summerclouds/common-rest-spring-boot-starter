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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.summerclouds.common.core.internal.SpringSummerCloudsCoreAutoConfiguration;
import org.summerclouds.common.core.util.StopWatch;
import org.summerclouds.common.junit.TestCase;
import org.summerclouds.common.rest.internal.SpringSummerCloudsRestAutoConfiguration;
import org.summerclouds.common.restree.impl.RestController;

@SpringBootTest(
        classes = {
            SpringSummerCloudsCoreAutoConfiguration.class,
            SpringSummerCloudsRestAutoConfiguration.class,
            TestingWebApplication.class,
            RestController.class
        },
        properties = {
            "org.summerclouds.scan.packages=org.summerclouds.common.rest.operation",
            "org.summerclouds.restree.enabled=true",
            "org.summerclouds.restree.public.enabled=true"
        })
@AutoConfigureMockMvc
public class RestreeTest extends TestCase {

    //	@Autowired
    //	private ServletContext servletContext;
    //	for (String sName : new EnumerationIterator<String>(servletContext.getServletNames())) {
    //	System.out.println("Servlet: " + sName);
    // }
    //
    // for (Map.Entry<String, ? extends ServletRegistration> reg :
    // servletContext.getServletRegistrations().entrySet()) {
    //	System.out.println("Reg: " + reg.getKey() + " " + reg.getValue().getMappings() + " " +
    // reg.getValue().getClassName());
    // }

    @Autowired private MockMvc mockMvc;

    @Test
    public void testPublic() throws Exception {
        MvcResult ret =
                this.mockMvc
                        .perform(get("/rest/public"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();
        String content = ret.getResponse().getContentAsString();
        assertTrue(content.contains("\"_timestamp\""));
    }

    @Test
    public void testPublicSleep() throws Exception {
        StopWatch watch = new StopWatch().start();
        MvcResult ret =
                this.mockMvc
                        .perform(post("/rest/public").param("_action", "sleep"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();
        watch.stop();
        String content = ret.getResponse().getContentAsString();
        assertTrue(content.contains("\"_timestamp\""));
        assertTrue(watch.getCurrentTime() > 900); // should be 1000
    }

    @Test
    public void testNotFound() throws Exception {
        MvcResult ret =
                this.mockMvc
                        .perform(get("/rest/notexists"))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andReturn();
        String content = ret.getResponse().getContentAsString();
        assertTrue(content.contains("\"_error\":404"));
    }
}
