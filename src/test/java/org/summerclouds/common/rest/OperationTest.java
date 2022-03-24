package org.summerclouds.common.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.summerclouds.common.core.internal.SpringSummerCloudsCoreAutoConfiguration;
import org.summerclouds.common.junit.TestCase;
import org.summerclouds.common.rest.internal.SpringSummerCloudsRestAutoConfiguration;
import org.summerclouds.common.rest.operation.OperationController;

@SpringBootTest(classes = {
		SpringSummerCloudsCoreAutoConfiguration.class,
		SpringSummerCloudsRestAutoConfiguration.class,
		TestingWebApplication.class,
		OperationController.class
		},
properties = { 
		"org.summerclouds.scan.packages=org.summerclouds.common.rest.operation",
		"org.summerclouds.operations.enabled=true",
}
)
@AutoConfigureMockMvc
public class OperationTest extends TestCase {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testOperationList() throws Exception {
		MvcResult ret = this.mockMvc.perform(get("/operation")).andDo(print()).andExpect(status().isOk()).andReturn();
		String content = ret.getResponse().getContentAsString();
		assertTrue(content.contains("operation://org.summerclouds.common.rest.operation.cmd1:0.0.0") );
		assertTrue(content.contains("operation://org.summerclouds.common.rest.operation.operation1:0.0.0") );
	}
	
	@Test
	public void testOperationDescription() throws Exception {
		this.mockMvc.perform(get("/operation/org.summerclouds.common.rest.operation.operation1:0.0.0")).andDo(print()).andExpect(status().isOk());
	}
	
	@Test
	public void testOperationExecute() throws Exception {
		this.mockMvc.perform(post("/operation/org.summerclouds.common.rest.operation.operation1:0.0.0")).andDo(print()).andExpect(status().isOk())
		.andExpect(jsonPath("$.message").value("[200,\"ok\"]"));
	}
	
	@Test
	public void testCommandList() throws Exception {
		MvcResult ret = this.mockMvc.perform(get("/cmd")).andDo(print()).andExpect(status().isOk()).andReturn();
		String content = ret.getResponse().getContentAsString();
		assertTrue(content.contains("\"path\":\"cmd1\"") );
		assertTrue(content.contains("\"version\":\"0.0.0\"") );
	}
	
	@Test
	public void testCommandExecute() throws Exception {
		String value = "t" + Math.random();
		MvcResult ret = this.mockMvc.perform(post("/cmd/cmd1").param("opt", value )).andDo(print()).andExpect(status().isOk()).andReturn();
		String content = ret.getResponse().getContentAsString();
		assertTrue(content.contains("Opt: " + value));
	}
	
	@Test
	public void testCommandError() throws Exception {
		MvcResult ret = this.mockMvc.perform(post("/cmd/cmd1").param("opt", "error" )).andDo(print()).andExpect(status().is(400)).andReturn();
		String content = ret.getResponse().getContentAsString();
		assertTrue(content.contains("[400,\"test error\"]"));
	}
	
}
