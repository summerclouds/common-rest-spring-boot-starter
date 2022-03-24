package org.summerclouds.common.restree.impl;

import javax.servlet.annotation.WebServlet;

import org.summerclouds.common.core.cfg.BeanRef;
import org.summerclouds.common.restree.api.RestApi;

@WebServlet(
		  name = "RESTREE",
		  description = "REST Tree Servlet",
		  urlPatterns = {"/rest"}
		)
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
